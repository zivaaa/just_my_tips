<template>
  <footer>
    <h2>Status: ({{ status }})</h2>
  </footer>
</template>

<script>
  export default {
    props: ["status"],
    name: "VFooter"
  }
</script>

<style lang="less">
  @import "VFooter";
</style>
