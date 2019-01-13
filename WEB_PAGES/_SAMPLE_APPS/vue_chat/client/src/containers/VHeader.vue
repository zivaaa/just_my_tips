<template>
  <header>
    <div class="head_wrap">
      <div class="logo-wrap">
        <p>ZChat</p>
      </div>
      <div class="header-content">
        <header-auth v-if="$store.state.authenticated"></header-auth>
        <header-non-auth v-else></header-non-auth>
      </div>
    </div>
  </header>
</template>

<script>
  import HeaderNonAuth from "../components/header_content/HeaderNonAuth";
  import HeaderAuth from "../components/header_content/HeaderAuth";

  export default {
    name: "VHeader",
    components: {HeaderAuth, HeaderNonAuth}
  }
</script>

<style lang="less">
  @import "VHeader";
</style>
