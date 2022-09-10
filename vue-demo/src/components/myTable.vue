<template>
  <div>
    <el-table :data="tableData">
      <el-table-column label="日期" prop="date" width="140"></el-table-column>
      <el-table-column label="姓名" prop="name" width="120"></el-table-column>
      <el-table-column label="地址" prop="address"></el-table-column>
    </el-table>
  </div>
</template>
<script>
export default {
  name: "",
  data() {
    const item = {
      date: '2020-05-20',
      name: 'fire',
      address: '长沙市雨花区金沙江路 245 路'
    };
    return {
      tableData: Array(20).fill(item)
    }
  },
  components: {},
  computed: {},
  // 生命周期函数
  beforeMount() {
  },
  mounted() {
  },
  methods: {},
  watch: {}
};
</script>
<style scoped>
</style>
