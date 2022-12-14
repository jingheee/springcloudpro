package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.RabbitInfo;
import com.atguigu.common.enume.OrderStatusEnum;
import com.atguigu.common.enume.SubmitOrderStatusEnum;
import com.atguigu.common.exception.NotStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;

//    @Value("${myRabbitmq.MQConfig.eventExchange}")
//    private String eventExchange;
//
//    @Value("${myRabbitmq.MQConfig.createOrder}")
//    private String createOrder;
//
//    @Value("${myRabbitmq.MQConfig.ReleaseOtherKey}")
//    private String ReleaseOtherKey;

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );
        return new PageUtils(page);
    }

    @Override // OrderServiceImpl
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        // ?????????????????????????????????????????????
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        // ????????????
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // ????????????request?????????????????????????????????????????????????????????????????????
        // ?????????????????????????????????????????????????????????request??????
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        // 1.???????????????????????????????????????
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // ???????????????????????????????????????????????????????????????request???????????????????????????????????????
            RequestContextHolder.setRequestAttributes(attributes);

            List<MemberAddressVo> address;
            try {
                address = memberFeignService.getAddress(memberRespVo.getId());
                confirmVo.setAddress(address);
            } catch (Exception e) {
                log.warn("\n?????????????????????????????? [???????????????????????????]");
            }
        }, executor);

        // 2. ?????????????????????????????????????????????????????????????????????
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // ?????????????????? RequestContextHolder.getRequestAttributes()
            RequestContextHolder.setRequestAttributes(attributes);

            // feign???????????????????????????????????? ?????????????????????
            // ??????????????????????????????
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> items = confirmVo.getItems();
            // ?????????????????????id
            List<Long> skus = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkuHasStock(skus);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {});
            if (data != null) {
                // ????????????id ??? ???????????????????????????map // ??????????????????map?????????
                Map<Long, Boolean> stocks = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(stocks);
            }
        }, executor);

        // 3.??????????????????
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4.????????????????????????????????????

        // TODO 5.???????????? ?????????????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        // redis???????????????id??????????????????????????????????????????????????????????????????????????????redis
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 10, TimeUnit.MINUTES);
        // ??????????????????????????????
        CompletableFuture.allOf(getAddressFuture, cartFuture).get();
        return confirmVo;
    }

    //	@GlobalTransactional // seata????????????TM
    @Transactional
    @Override // OrderServiceImpl
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        // ??????????????????????????????
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo submitVo = new SubmitOrderResponseVo();
        // 0?????????
        submitVo.setCode(0);
        // ????????????????????????,?????????,?????????,?????????
        MemberRespVo MemberRespVo = LoginUserInterceptor.threadLocal.get();

        // 1. ???????????? [?????????????????????] ?????? 0 or 1
        // 0 ?????????????????? 1????????????
        String script = "if redis.call('get',KEYS[1]) == ARGV[1]" +
                "then return redis.call('del',KEYS[1]) " +
                "else return 0 " +
                "end";
        String orderToken = vo.getOrderToken();

        // ?????????????????? ????????????
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + MemberRespVo.getId()),
                orderToken);
        if (result == 0L) { // ??????????????????
            submitVo.setCode(SubmitOrderStatusEnum.TOKENERROR.getCode());
        } else {  // ??????????????????
            // 1 .????????????????????? // ?????????????????????????????????
            OrderCreateTo order = createOrder();
            // 2. ??????
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal voPayPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(voPayPrice).doubleValue()) < 0.01) {
                // ??????????????????
                // 3.???????????? ????????????

                // 4.????????????
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    // ?????????skuId ??????skuId??????????????????
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());

                lockVo.setLocks(locks);
                // ???????????????
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // ???????????? ???????????? ???MQ?????????????????????????????????????????????
                    submitVo.setOrderEntity(order.getOrder());
                    // ??????????????????????????????????????????MQ??????????????????
                    rabbitTemplate.convertAndSend(RabbitInfo.Order.exchange,
                            RabbitInfo.Order.delayQueue,
                            order.getOrder());
                    saveOrder(order);
//					int i = 10/0;
                } else {
                    // ????????????
                    String msg = (String) r.get("msg");
                    throw new NotStockException(msg);
                }
            } else {

                // ??????????????????
                submitVo.setCode(SubmitOrderStatusEnum.CHECKPRICE.getCode());
                log.warn("????????????");
            }
        }
        return submitVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        log.info("\n???????????????????????????--???????????????:" + entity.getOrderSn());
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        OrderEntity orderEntity = this.getById(entity.getId());
        //??????????????????????????????????????????????????????????????????????????????
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            // ?????????MQ??????????????????????????????????????????
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                // ???????????? 100% ????????? ?????????????????????????????????????????????
                // ????????????????????? ?????????????????????????????????
                rabbitTemplate.convertAndSend(RabbitInfo.Order.exchange,
                        RabbitInfo.Order.baseRoutingKey, orderTo);
            } catch (AmqpException e) {
                // ?????????????????????????????????????????????.
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);
        // ??????2????????????????????????
        payVo.setTotal_amount(order.getTotalAmount().add(order.getFreightAmount() == null ? new BigDecimal("0") : order.getFreightAmount()).setScale(2, BigDecimal.ROUND_UP).toString());
        payVo.setOut_trade_no(order.getOrderSn());
        List<OrderItemEntity> entities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
        payVo.setSubject("glmall");
        payVo.setBody("glmall");
        if (null != entities.get(0).getSkuName() && entities.get(0).getSkuName().length() > 1) {
//			payVo.setSubject(entities.get(0).getSkuName());
//			payVo.setBody(entities.get(0).getSkuName());
            payVo.setSubject("glmall");
            payVo.setBody("glmall");
        }
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo respVo = LoginUserInterceptor.threadLocal.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),

                // ????????????????????????????????? [????????????]
                new QueryWrapper<OrderEntity>().eq("member_id", respVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            // ??????????????????????????????????????????
            List<OrderItemEntity> orderSn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(orderSn);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(order_sn);
        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo vo) {

        // 1.??????????????????
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        //		TRADE_SUCCESS
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());
        infoEntity.setSubject(vo.getSubject());
        infoEntity.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        infoEntity.setCreateTime(vo.getGmt_create());
        paymentInfoService.save(infoEntity);

        // 2.????????????????????????
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            // ????????????
            String orderSn = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    @Override
    public void createSecKillOrder(SecKillOrderTo secKillOrderTo) {
        log.info("\n??????????????????");
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(secKillOrderTo.getOrderSn());
        entity.setMemberId(secKillOrderTo.getMemberId());
        entity.setCreateTime(new Date());
        entity.setPayAmount(secKillOrderTo.getSeckillPrice());
        entity.setTotalAmount(secKillOrderTo.getSeckillPrice());
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setPayType(1);
        // TODO ????????????????????????
        BigDecimal price = secKillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + secKillOrderTo.getNum()));
        entity.setPayAmount(price);

        this.save(entity);

        // ?????????????????????
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(secKillOrderTo.getOrderSn());
        itemEntity.setRealAmount(price);
        itemEntity.setOrderId(entity.getId());
        itemEntity.setSkuQuantity(secKillOrderTo.getNum());
        R info = productFeignService.getSpuInfoBySkuId(secKillOrderTo.getSkuId());
        SpuInfoVo spuInfo = info.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(spuInfo.getId());
        itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        itemEntity.setSpuName(spuInfo.getSpuName());
        itemEntity.setCategoryId(spuInfo.getCatalogId());
        itemEntity.setGiftGrowth(secKillOrderTo.getSeckillPrice().multiply(new BigDecimal(secKillOrderTo.getNum())).intValue());
        itemEntity.setGiftIntegration(secKillOrderTo.getSeckillPrice().multiply(new BigDecimal(secKillOrderTo.getNum())).intValue());
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        orderItemService.save(itemEntity);
    }

    /**
     * ????????????????????????
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItems = orderItems.stream().map(item -> {
            item.setOrderId(orderEntity.getId());
            item.setSpuName(item.getSpuName());
            item.setOrderSn(order.getOrder().getOrderSn());
            return item;
        }).collect(Collectors.toList());
        orderItemService.saveBatch(orderItems);
    }

    /**
     * ????????????
     */
    private OrderCreateTo createOrder() {

        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1. ?????????????????????
        String orderSn = IdWorker.getTimeId();
        // ????????????????????????????????????????????????
        OrderEntity orderEntity = buildOrderSn(orderSn);

        // 2. ?????????????????????   // ???????????????????????????????????????????????????
        List<OrderItemEntity> items = buildOrderItems(orderSn);

        // 3.???????????????????????????	???????????? ???????????? ????????????????????????????????????????????????
        computerPrice(orderEntity, items);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(items);
        return orderCreateTo;
    }

    /**
     * ??????  ???????????????????????????????????????????????????????????????????????????????????????
     */
    private void computerPrice(OrderEntity orderEntity, List<OrderItemEntity> items) {

        // ?????????????????????????????????
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");

        // ??????
        BigDecimal totalPrice = new BigDecimal("0.0");
        for (OrderItemEntity item : items) {  // ?????????????????????????????????????????????????????????????????????????????????
            // ??????????????????
            coupon = coupon.add(item.getCouponAmount());
            // ?????????????????????
            integration = integration.add(item.getIntegrationAmount());
            // ???????????????
            promotion = promotion.add(item.getPromotionAmount());
            BigDecimal realAmount = item.getRealAmount();
            totalPrice = totalPrice.add(realAmount);

            // ?????????????????????????????????
            gift.add(new BigDecimal(item.getGiftIntegration().toString()));
            growth.add(new BigDecimal(item.getGiftGrowth().toString()));
        }
        // 1.?????????????????? ?????????????????????
        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));

        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        // ????????????????????????
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());

        // ???????????????????????????
        orderEntity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());
    }

    /**
     * ??? orderSn ????????????????????????
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // ???????????????????????????????????????????????? ?????????????????????????????????????????????
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        List<OrderItemEntity> itemEntities = null;
        if (cartItems != null && cartItems.size() > 0) {
            itemEntities = cartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return itemEntities;
    }

    /**
     * ????????????????????????
     */ // OrderServiceImpl
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1.??????????????? ?????????
        // ?????????items????????????

        // 2.??????spu??????
        Long skuId = cartItem.getSkuId();
        // ????????????spu?????????
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(spuInfo.getId());
        itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        itemEntity.setSpuName(spuInfo.getSpuName());
        itemEntity.setCategoryId(spuInfo.getCatalogId());

        // 3.?????????sku??????
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        // ????????????????????????????????????????????????????????????????????????
        // ??????list????????????string
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        // 4.???????????? ?????????????????????????????? ???????????????
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());

        // 5.???????????????????????? ????????????
        itemEntity.setPromotionAmount(new BigDecimal("0.0")); // ????????????
        itemEntity.setCouponAmount(new BigDecimal("0.0")); // ?????????
        itemEntity.setIntegrationAmount(new BigDecimal("0.0")); // ??????

        // ????????????????????????
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        // ???????????????????????????
        BigDecimal subtract =
                orign.subtract(itemEntity.getCouponAmount()) // ????????????????????????????????????coupon??????????????????sku?????????
                        .subtract(itemEntity.getPromotionAmount()) // ????????????
                        .subtract(itemEntity.getIntegrationAmount()); // ??????/??????
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }

    /**
     * ??????????????????
     */
    private OrderEntity buildOrderSn(String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setCreateTime(new Date());
        entity.setCommentTime(new Date());
        entity.setReceiveTime(new Date());
        entity.setDeliveryTime(new Date());
        MemberRespVo respVo = LoginUserInterceptor.threadLocal.get();
        entity.setMemberId(respVo.getId());
        entity.setMemberUsername(respVo.getUsername());
        entity.setBillReceiverEmail(respVo.getEmail());
        // 2. ????????????????????????
        OrderSubmitVo submitVo = confirmVoThreadLocal.get();
        R fare = wmsFeignService.getFare(submitVo.getAddrId());//??????
        FareVo resp = fare.getData(new TypeReference<FareVo>() {
        });
        entity.setFreightAmount(resp.getFare());
        entity.setReceiverCity(resp.getMemberAddressVo().getCity());
        entity.setReceiverDetailAddress(resp.getMemberAddressVo().getDetailAddress());
        entity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setReceiverPhone(resp.getMemberAddressVo().getPhone());
        entity.setReceiverName(resp.getMemberAddressVo().getName());
        entity.setReceiverPostCode(resp.getMemberAddressVo().getPostCode());
        entity.setReceiverProvince(resp.getMemberAddressVo().getProvince());
        entity.setReceiverRegion(resp.getMemberAddressVo().getRegion());
        // ???????????????????????????
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);//??????????????????
        return entity;
    }
}