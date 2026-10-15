<template>
  <div class="box-item">
    <ul>
      <li
      v-for="(item, index) in orderNum"
      :key="item.toString() + index.toString() + getRandomNumber(1, 10000)"
      :class="{ 'number-item': !isNaN(item), 'mark-item': isNaN(item) }"
      >
      <!--<div class="digit">{{ getDigit(index) }}</div>-->
      <span v-if="!isNaN(item)">
        <i ref="numberItem" style="z-index: 2;">0123456789</i>
        <span class="splitLine"></span>
      </span>
      <span v-else class="comma">{{ item }}</span>
      </li>
    </ul>
    <i class="unit" style="color: #eb6f2e">瓶</i>
  </div>
</template>
<script>
export default {
  name: 'FlipNumber',
  props: {
    // 具体数值
    number: {
      type: Number,
      default() {
        return 0
      }
    },
    // 数字更新的时间
    time: {
      type: Number,
      default() {
        return 1
      }
    },
    // 数字最小长度，长度不足时根据fillZero配置补0
    numberLength: {
      type: Number,
      default: 10
    },
    // 不足numberLength时是否在前面补0
    fillZero: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      orderNum: []
    }
  },
  watch: {
    number(newVal, oldVal) {
      this.orderNum = this.transNum2ShowValue(newVal) // 这里输入数字即可调用
      this.flip(oldVal)
    }
  },
  mounted() {
    this.orderNum = this.transNum2ShowValue(this.number) // 这里输入数字即可调用
    this.flip()
  },
  methods: {
    getDigit(index) {

    },
    flip(oldVal) {
      const self = this
      // orderNum更改之后，页面会重新渲染，即数字全部归0，如果不赋值一次旧，会出现全0的情况
      this.$nextTick(() => {
        // 重新渲染之后先给<i>赋旧值
        if (oldVal) {
          this.setNumberTransform(this.transNum2ShowValue(oldVal))
        }
        // 再赋新值，这样就会出现变化的效果
        setTimeout(() => {
          self.setNumberTransform(this.orderNum)
        }, this.time * 1500)
      })
    },
    // 设置文字滚动
    setNumberTransform(numStr) {
      console.log(numStr, 'numStr')
      const numberItems = this.$refs.numberItem
      const numberArr = numStr.filter(item => !isNaN(item))
      for (let index = 1; index <= numberArr.length; index++) {
        // 倒序赋值，保证出现位数变化的时候，0出现在第一个，比如从9999变为10001，会先变成09999，在变成10001
        const elem = numberItems[numberItems.length - index]
        console.log(elem, 'elem ')
        elem.style.transform = `translate(-50%, -${numberArr[numberArr.length - index] * 10}%)`
      }
    },
    getRandomNumber(min, max) {
      return Math.floor(Math.random() * (max - min + 1) + min)
    },
    // 处理传过来的具体值value
    transNum2ShowValue(num) {
      if (this.fillZero) {
        if (num.length < this.numberLength) {
          num = this.preZeroFill(num, this.numberLength)
        } else {
          num = this.numberWithDot(num)
        }
      } else {
        num = this.numberWithDot(num)
      }
      console.log(num, '-------------num')
      return num.split('')
    },
    numberWithDot(number) {
      const str = []
      if (typeof number === 'number') {
        number = String(number)
      }
      if (number.length <= 3) {
        return number
      }
      if(number.length <= 6) {
        number = '00' + number
      }
      console.log(number, 'numbernumbernumber')
      String(number).split('').forEach((item, index) => {
        console.log(item, 'item')
          if (index !== 0 && index % 3 === 0) {
            str.push(',', item)
          } else {
            str.push(item)
          }
        })

      return str.join('')
    },
    preZeroFill(num, size) {
      if (num >= Math.pow(10, size)) {
        // 如果num本身位数不小于size位
        return num.toString()
      } else {
        var _str = Array(size + 1).join('0') + num
        return _str.slice(_str.length - size)
      }
    }
  }
}
</script>
<style scoped>
/*具体值value总量滚动数字设置*/
.box-item {
  position: relative;
  left: 50%; /* 将子元素左边界移动到父元素的中间位置 */
  transform: translateX(-50%); /* 使用负的自身宽度的一半来调整位置，实现水平居中 */
  height: 75px;
  font-size: 27px;
  line-height: 20px;
  text-align: center;
  list-style: none;
  color: #fff;
  writing-mode: vertical-rl;
  text-orientation: upright;
  /*文字禁止编辑*/
  -moz-user-select: none; /*火狐*/
  -webkit-user-select: none; /*webkit浏览器*/
  -ms-user-select: none; /*IE10*/
  -khtml-user-select: none; /*早期浏览器*/
  user-select: none;
  /* overflow: hidden; */
  & > i {
    position: absolute;
    color: #fff;
    font-size: 20px;
    right: -20px;
    bottom: 0;
  }
}
.splitLine {
  position: absolute;
  width: 52px;
  height: 0;
  border: 4px solid #1a1f51;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
}

/* 默认逗号设置 */
.mark-item {
  width: 10px;
  height: 75px;
  margin-right: 5px;
  line-height: 10px;
  font-size: 24px;
  position: relative;
  & > span {
    position: absolute;
    width: 100%;
    bottom: 0;
    writing-mode: vertical-rl;
    text-orientation: upright;
  }
}
/*滚动数字设置*/
.number-item {
  position: relative;
  width: 51px;
  height: 75px;
  background: #eb6f2e;
  list-style: none;
  margin-right: 8px;
  border-radius: 10px;
  .digit {
    position: absolute;
    top: -40%;
    left: 0;
  }
  & > span {
    position: relative;
    display: inline-block;
    width: 100%;
    height: 100%;
    writing-mode: vertical-lr;
    text-orientation: upright;
    overflow: hidden;
    & > i {
      font-style: normal;
      position: absolute;
      font-size: 60px;
      top: 2%;
      left: 50%;
      transform: translate(-50%, 0);
      transition: transform 1s ease-in-out;
      letter-spacing: 10px;
      font-weight: bold;
    }
  }
}
.number-item:last-child {
  margin-right: 0;
}
</style>
