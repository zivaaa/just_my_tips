<template>
  <transition name="fade">
    <div class="modal" v-if="showing" >
      <div class="modal-content">
        <div class="modal-close-btn" v-if="disposable" @click="hideModal">X</div>
        <div class="modal-header">
          <slot name="header"></slot>
        </div>
        <div class="modal-body" slot>
          <slot></slot>
        </div>
        <div class="modal-footer" slot="footer">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
  export default {
    name: "Modal",
    props: {
      disposable: Boolean,
      show: Boolean
    },
    mounted() {
      console.log("mounted");
      if (!this.disposable) {
        this.showing = !!this.show
      }
    },
    data() {
      return {
        showing: false
      }
    },
    methods: {
      showModal() {
        this.showing = true;
      },
      hideModal() {
        this.showing = false;
      }
    },
    watch: {
      show: function(newVal, oldVal) { // watch it
        if (!this.disposable) {
          this.showing = newVal
        }
      }
    }
  }
</script>

<style lang="less">
  @import "Modal";
</style>
