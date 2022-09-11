import cn.hutool.core.util.IdUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.atguigu.common.config.S3Config;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class S3Test {
    @Test
    void S3tet() {
        AmazonS3 client = S3Config.client();
        Bucket demo = client.createBucket("demo");
        System.out.println(demo);
    }

    final static int CNT = 10_000_000;

    @Test
    void idTest() {
        List<String> list = new ArrayList<>(CNT);
        for (int i = 0; i < CNT; i++) {
            list.add(IdUtil.fastUUID());
        }
        System.out.println(list.size());
    }
}
