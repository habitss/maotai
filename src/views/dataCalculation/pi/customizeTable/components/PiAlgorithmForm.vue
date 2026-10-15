<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <a-row :gutter="[0,16]">
      <!--**************计算信号********************-->
      <a-col class="gutter-row" :span="13">
        <div class="gutter-box-top">
          <div class="box-piece-title">计算信号</div>
          <a-divider class="divider-style"/>
          <div class="box-piece-content">
            <div class=" height-30">
              <span class="margin-right-10">点位类型</span>
              <!-- <a-radio-group v-model:value="calculateSignal.pointType" name="radioGroup" @change="radioGroupChange">
                <a-radio value="0">自动化点位</a-radio>
                <a-radio value="1">时间</a-radio>
              </a-radio-group> -->
              <a-radio-group v-model:value="calculateSignal.pointType" button-style="solid" @change="pointTypeChange">
                <a-radio-button :value="0" title="自动化点位">自动化点位</a-radio-button>
                <a-radio-button :value="1" title="时间">时间</a-radio-button>
              </a-radio-group>
            </div>
            <div class="height-30" v-if="calculateSignal.pointType === 0">
              <!--自动化点位-->
              <span>当&ensp;前&ensp;点</span>
              <!--基础点位(步骤)-->
              <!-- <a-select show-search option-filter-prop="label" v-model:value="calculateSignal.pointCode" :size="size" class="width-200 margin-left-10" :options="pointStepArr" placeholder="请选择" @change="pointStepChange"></a-select> -->
              <!-- <a-select show-search :filter-option="filterOption" v-model:value="calculateSignal.pointCode" :size="size" class="width-200 margin-left-10" :options="pointStepArr" placeholder="请选择" @change="pointStepChange" allowClear></a-select> -->
              <a-select show-search option-filter-prop="label" v-model:value="calculateSignal.pointCode" :size="size" class="width-200 margin-left-10" :options="pointArr" placeholder="请选择" @change="pointStepChange" allowClear></a-select>
              <a-select v-model:value="calculateSignal.calculateType" :size="size" class="width-100 margin-left-10" :options="countConditionArr" placeholder="请选择" allowClear></a-select>
              
              <!--步骤-->
              <a-select v-model:value="calculateSignal.calculateValue" :size="size" class="width-100 margin-left-10" :options="stepArr" placeholder="请选择" v-if="isStep" allowClear></a-select>
              <a-input v-model:value="calculateSignal.calculateValue" placeholder="请输入" :size="size" class="width-90 margin-left-10" v-else allowClear/>
              <DownCircleOutlined class="width-50 margin-left-10 icon-style" title="添加" @click="addCalculateSignal"/>
            </div>
            <div class="height-30" v-if="calculateSignal.pointType === 1">
              <!--时间-->
              <span>时间类型</span>
              <a-select v-model:value="calculateSignal.timeType" :size="size" class="width-100 margin-left-10" :options="timeTypeArr" placeholder="请选择" allowClear @change="timeTypeChange"></a-select>
              <a-time-picker v-if="calculateSignal.timeType === 2" v-model:value="calculateSignal.timeComment" :size="size" class="width-100 margin-left-10" placeholder="请选择" format="HH:mm" valueFormat="HH:mm" allowClear></a-time-picker>
            </div>

            <div>
              <span class="margin-right-10">取值类型</span>
              <a-radio-group v-model:value="calculateSignal.stepType" name="radioGroup" @change="stepTypeChange">
                <a-radio value="0">瞬时取值</a-radio>
                <a-radio value="1">范围取值</a-radio>
                <a-radio value="2">监控取值</a-radio>
              </a-radio-group>
            </div>

            <div style="margin-top: 10px;">计算信号描述</div>
            <div class="text-count-content" style="height: 124px;">
              <div>
                {{calculateSignal.explanation}}
              </div>
            </div>
          </div>
        </div>
      </a-col>

      <!--**************取数信号********************-->
      <a-col class="gutter-row" :span="11">
        <div class="gutter-box-top">
          <div class="box-piece-title">取数信号</div>
          <a-divider class="divider-style" />
          <div class="box-piece-content">
            <div>
              <span class="margin-right-10">适用于</span>
              <a-radio-group v-model:value="applyTo" name="radioGroup" @change="radioGroupChange">
                <a-radio value="0">计算信号</a-radio>
                <a-radio value="1">取数步骤</a-radio>
              </a-radio-group>
            </div>
            <div style="height: 140px;overflow-y: auto;">
              <div class="height-80" v-for="(val, index) in retrievalSignal" :key="index" style="margin-bottom: 6px;">
                <div>
                  <PlusCircleOutlined @click="addRetrievalSignal" class="width-25 icon-style" title="添加" :class="{icon_add_visibility: index != 0}"/>
                  <span>当点位</span>
                  <!--非步骤点位-->
                  <!-- <a-select show-search option-filter-prop="label" v-model:value="val.pointCode" :size="size" class="width-200 margin-left-10" :options="pointNotStepArr" placeholder="请选择" allowClear></a-select> -->
                  <!--全量点位-->
                  <a-select show-search option-filter-prop="label" v-model:value="val.pointCode" :size="size" class="width-170 margin-left-5" :options="pointArr" placeholder="请选择" allowClear></a-select>
                  <a-select v-model:value="val.calculateType" :size="size" class="width-70 margin-left-5" :options="val.fetchDynamicConditionArr" placeholder="请选择" allowClear></a-select>
                  
                  <a-radio-group v-model:value="val.valueType" button-style="solid" @change="valueTypeChange(val)" class="margin-left-5">
                    <a-radio-button value="0" title="值">值</a-radio-button>
                    <a-radio-button value="1" title="点位">点位</a-radio-button>
                    <a-radio-button value="2" title="字典">字典</a-radio-button>
                  </a-radio-group>
                </div>
                <div style="margin-top: 3px;padding-left: 67px;">
                  <!--值-->
                  <a-input v-model:value="val.calculateValue" placeholder="请输入" :size="size" class="width-170 margin-left-5" v-if="val.valueType == '0'" allowClear/>
                  <!--点位-->
                  <a-select v-model:value="val.calculateValue" show-search option-filter-prop="label" :size="size" class="width-170 margin-left-5" v-if="val.valueType == '1'" :options="pointArr" placeholder="请选择" allowClear></a-select>
                  <!--字典-->
                  <a-select v-model:value="val.calculateValue" show-search option-filter-prop="label" :size="size" class="width-170 margin-left-5" v-if="val.valueType == '2'" :options="dictOptions" placeholder="请选择" allowClear></a-select>
                  
                  <a-select v-model:value="val.advanceDelay" :size="size" class="width-90 margin-left-5" :options="computationAdvanceArr" placeholder="请选择" allowClear></a-select>
                  <a-input v-model:value="val.advanceTime" placeholder="请输入" :size="size" class="width-70 margin-left-5"/>
                  <span>秒</span>
                  <!--配置隐藏(值 1显示0不显示 默认显示)-->
                  <a-switch v-model:checked="val.display" v-if="isDisplay" :checkedValue="'1'" :unCheckedValue="'0'" class="margin-left-5"/>
                  <DownCircleOutlined class="width-50 margin-left-10 icon-style" title="添加" :class="{icon_add_visibility: index != 0}" @click="addRetrievalSignalData"/>
                </div>
                
              </div>
            </div>
            <div class="text-content" style="height: 85px;">
              <!-- <div v-for="(item, index) in retrievalSignalHistory" :key="index">{{index}}.{{item.pointName}}{{item.calculateType}}{{item.calculateValue}}</div> -->
              <div v-for="(item, index) in retrievalSignalHistory" :key="index">
                {{index+1}}.
                {{item.applyToStr}} 
                <template v-for="(strItem, index) in item.strArr" :key="index">
                  <span v-if="strItem.display=='1'">{{ strItem.str }}</span>
                  <span v-if="strItem.display=='0'" style="color: silver;">{{ strItem.str }}</span>
                </template>
                
                <span class="del-signal" @click="delRetrievalSignal(index)" style="cursor: pointer;color:red" title="删除"><CloseCircleOutlined/></span>
              </div>
            </div>
          </div>
        </div>
      </a-col>

      <!--**************取数步骤********************-->
      <a-col class="gutter-row" :span="13">
        <div class="gutter-box">
          <div class="box-piece-title">取数步骤</div>
          <a-divider class="divider-style"/>
          <div class="box-piece-content">
             <!--范围取值-->
             <div style="height: 140px;display: flex;align-items: flex-start;flex-direction: column;justify-content: space-evenly;">
               <div>
                <span>开始步骤</span>
                <a-select v-model:value="retrievalStepStart.stepValueType" show-search option-filter-prop="label" :size="size" class="width-100 margin-left-10" @change="startStepChange" :options="stepValueTypeArr" placeholder="请选择" :disabled="calculateSignal.stepType=='0'" allowClear></a-select>
                <a-select v-model:value="retrievalStepStart.pointCode" show-search option-filter-prop="label" :size="size" class="width-150 margin-left-10" :options="startStepList" placeholder="请选择" :disabled="calculateSignal.stepType=='0' || retrievalStepStart.stepValueType=='1'" allowClear></a-select>
                <a-select v-model:value="retrievalStepStart.calculateType" :size="size" class="width-100 margin-left-10" :options="countConditionArr" placeholder="请选择" :disabled="calculateSignal.stepType=='0' || retrievalStepStart.stepValueType=='1' || retrievalStepStart.stepValueType=='3'" allowClear></a-select>
                <a-input v-model:value="retrievalStepStart.calculateValue" placeholder="请输入" :size="size" class="width-90 margin-left-10" :disabled="calculateSignal.stepType=='0' || retrievalStepStart.stepValueType=='1' || retrievalStepStart.stepValueType=='3'" allowClear/>
                <!-- <span class ="margin-left-10 margin-right-10">提前或延后</span> -->
                <a-select v-model:value="retrievalStepStart.advanceDelay" :size="size" class="width-100 margin-left-10" :options="advanceOrDelayArr" placeholder="请选择" :disabled="calculateSignal.stepType=='0'" allowClear></a-select>
                <a-input v-model:value="retrievalStepStart.advanceTime" placeholder="请输入" :size="size" class="width-50 margin-left-10" :disabled="calculateSignal.stepType=='0'"/>
                <span>秒</span>

              </div>
              <div>
                <span>结束步骤</span>
                <a-select v-model:value="retrievalStepEnd.stepValueType" show-search option-filter-prop="label" :size="size" class="width-100 margin-left-10" @change="endStepChange" :options="stepValueTypeArr" placeholder="请选择" :disabled="calculateSignal.stepType=='0'" allowClear></a-select>
                <a-select v-model:value="retrievalStepEnd.pointCode" show-search option-filter-prop="label" :size="size" class="width-150 margin-left-10" :options="endStepList" placeholder="请选择" :disabled="calculateSignal.stepType=='0' || retrievalStepEnd.stepValueType=='1'" allowClear></a-select>
                <a-select v-model:value="retrievalStepEnd.calculateType" :size="size" class="width-100 margin-left-10" :options="countConditionArr" placeholder="请选择" :disabled="calculateSignal.stepType=='0' || retrievalStepEnd.stepValueType=='1' || retrievalStepEnd.stepValueType=='3'" allowClear></a-select>
                <a-input v-model:value="retrievalStepEnd.calculateValue" placeholder="请输入" :size="size" class="width-90 margin-left-10" :disabled="calculateSignal.stepType=='0' || retrievalStepEnd.stepValueType=='1' || retrievalStepEnd.stepValueType=='3'" allowClear/>
                <!-- <span class ="margin-left-10 margin-right-10">提前或延后</span> -->
                <a-select v-model:value="retrievalStepEnd.advanceDelay" :size="size" class="width-100 margin-left-10" :options="advanceOrDelayArr" placeholder="请选择" :disabled="calculateSignal.stepType=='0'" allowClear></a-select>
                <a-input v-model:value="retrievalStepEnd.advanceTime" placeholder="请输入" :size="size" class="width-50 margin-left-10" :disabled="calculateSignal.stepType=='0'"/>
                <span>秒</span>
              </div>
              <div>
                <span>频&#12288;&#12288;率</span>
                <a-input v-model:value="retrievalStepStart.frequency" placeholder="请输入" :size="size" class="width-80 margin-left-10" :disabled="calculateSignal.stepType=='0'"/>
                <span>秒</span>
              </div>
              <div>
                <span>是否切割</span>
                <a-select v-model:value="retrievalStepStart.isSlicing" :size="size" class="width-80 margin-left-10" :options="isSlicingArr" :disabled="calculateSignal.stepType=='0'"></a-select>
              </div>
             </div>
          </div>
        </div>
      </a-col>

      <!--**************唯一约束********************-->
      <a-col class="gutter-row" :span="11">
        <div class="gutter-box">
          <div class="box-piece-title">唯一约束</div>
          <a-divider class="divider-style" />
          <div class="box-piece-content" style="height: 140px;">
            <div class="height-30">
              <span class="margin-right-10">提前/延后</span>
              <a-select v-model:value="calculateSignal.advanceDelay" :size="size" class="width-100" :options="advanceOrDelayArr" placeholder="请选择" allowClear></a-select>
              <a-input v-model:value="calculateSignal.advanceTime" placeholder="请输入" :size="size" class="width-100"/>
              <span class="margin-left-10">秒</span>
              <PlusCircleOutlined @click="addUnique" class="width-25 icon-style margin-left-10" title="添加"/>
            </div>
            <div style="height:110px;overflow-y: auto;">
              <div class="height-60" v-for="(val, index) in uniqueList" :key="index" style="margin-top: 5px;">
                <!-- <span class="margin-right-10">{{val.colName}}=</span> -->
                <a-select v-model:value="val.colCode" show-search option-filter-prop="label" :size="size" class="width-200 margin-right-10" :options="piFieldAllList" placeholder="请选择" @change="uniqueChange(val)" allowClear></a-select>
                <a-select v-model:value="val.tagCode" show-search option-filter-prop="label" :size="size" :disabled="isUniqueList(val.uniqueValue,val.yearHour)" class="width-200" :options="pointArr" placeholder="请选择" allowClear></a-select>
                <span class="margin-left-10 margin-right-10">或</span>
                <a-input v-model:value="val.uniqueValue" placeholder="请输入" :size="size" class="width-80" :disabled="isUniqueList(val.tagCode,val.yearHour)" allowClear/>
                <span class="margin-left-10 margin-right-10">或</span>
                <a-select v-model:value="val.yearHour" :size="size" class="width-120" :disabled="isUniqueList(val.tagCode,val.uniqueValue)" :options="yearHourArr" placeholder="请选择" allowClear @change="function(){val.frequency=''}"></a-select>
                <!-- <div> -->
                <a-select v-if="val.yearHour == 'minFreFun'" v-model:value="val.frequency" :size="size" class="width-80" :disabled="isUniqueList(val.tagCode,val.uniqueValue)" :options="minFreArr" placeholder="请选择" allowClear></a-select>
                <a-time-picker v-if="val.yearHour == 'timeFreFun'" v-model:value="val.frequency" :size="size" class="width-80" :disabled="isUniqueList(val.tagCode,val.uniqueValue)" placeholder="请选择" format="HH:mm" valueFormat="HH:mm" allowClear></a-time-picker>
                <a-select v-model:value="val.monitorModelId" title="模型名称" show-search option-filter-prop="label" :size="size" :options="resultModelList" class="width-150 margin-right-10" placeholder="请选择模型名称" allowClear></a-select>
                <span class="del-signal" @click="delUnique(index)" style="cursor: pointer;color:red" title="删除"><CloseCircleOutlined/></span>
                <!-- </div> -->
              </div>

            </div> 

          </div>
        </div>
      </a-col>
    </a-row>

    <!--**************计算模型********************-->
    <a-row style="margin-top: 20px;" v-if="calculateSignal.stepType=='0' || calculateSignal.stepType =='1'">
      <a-col class="gutter-row" :span="24">
        <div class="gutter-box-bottom">
          <div class="box-piece-title">计算模型</div>
          <a-divider class="divider-style" />
          <div class="box-piece-content">
            <div style="width:100%;display: flex;justify-content: space-between;">
              <div>
                <div class="height-35">
                  <span style="padding-right: 10px;">取值来源</span>
                  <a-radio-group v-model:value="computationModel.valueSource" name="radioGroup" @change="valueSourceChange" :disabled="calculateSignal.stepType=='1'">
                    <a-radio value="0">基础点位</a-radio>
                    <a-radio value="1">PI日报</a-radio>
                  </a-radio-group>
                </div>
                <div class="height-35">
                  <span style="padding-right: 10px;">是否覆盖</span>
                  <a-radio-group v-model:value="computationModel.ifCover" name="radioGroup">
                    <a-radio value="1">是</a-radio>
                    <a-radio value="0">否</a-radio>
                  </a-radio-group>
                </div>
                <div class="height-35">
                  <span style="padding-right: 10px;">汉化模型</span>
                  <a-select show-search option-filter-prop="label" v-model:value="computationModel.sinicizedDic" :size="size" class="width-200" :options="dictOptions" placeholder="请选择" allowClear></a-select>
                </div>
                <div class="height-35">
                  <span style="padding-right: 10px;">结果类型</span>
                  <a-select v-model:value="computationModel.resultType" :size="size" style="width: 100px;" :options="resultTypeArr" placeholder="请选择" allowClear></a-select>
                  <span class="margin-left-10" style="color:red"><ExclamationCircleOutlined />&ensp;仅适用时间类型PI计算</span>
                </div>
                <div class="height-35">
                  <span class="margin-right-10">提前/延后</span>
                  <a-select v-model:value="computationModel.advanceDelay" :size="size" class="width-100" :options="advanceOrDelayArr" placeholder="请选择" allowClear></a-select>
                  <a-input v-model:value="computationModel.advanceTime" placeholder="请输入" :size="size" class="width-90"/>
                  <span class="margin-left-10">秒</span>
                </div>
                <!--排序方式-->
                <div class="height-35" v-if="(computationModel.valueSource==1 && calculateSignal.stepType==1) || computationShow.sortField">
                  <span class="margin-right-10">排序字段</span>
                  <a-select v-model:value="computationModel.sortField" show-search option-filter-prop="label" :size="size" class="width-200 margin-right-10" :options="countFieldList" placeholder="请选择" :disabled="computationModel.notEdit" allowClear></a-select>
                  <a-select v-model:value="computationModel.sortMode" show-search option-filter-prop="label" :size="size" class="width-100" :options="sortModeList" placeholder="请选择" :disabled="computationModel.notEdit" allowClear></a-select>
                </div>
                <!--过滤字段-->
                <div class="height-35" v-if="(computationModel.valueSource==1 && calculateSignal.stepType==1) || computationShow.sortField">
                  <span class="margin-right-10">过滤字段</span>
                  <a-select v-model:value="computationSpecialModel.filterField" show-search option-filter-prop="label"  mode="multiple" :size="size" class="margin-right-10" style="width:310px;" :options="countFieldList" placeholder="请选择" :disabled="computationModel.notEdit" allowClear></a-select>
                </div>
                <!--模型名称-->
                <div class="height-35">
                  <span class="margin-right-10" title="结果模型转换名称">模型名称</span>
                  <a-select v-model:value="computationModel.resultModelId" show-search option-filter-prop="label" :size="size" :options="resultModelList" class="width-200" placeholder="请选择" @change="(val)=> resultModelChange(val, computationModel)" allowClear></a-select>
                </div>
              </div>
              <div style="width:78%">
                <!-- <div class="height-35"></div> -->
                <!-- <div class="height-35"></div> -->
                <div class="height-35">
                  <a-button class="margin-right-10" title="当前时间(timeFun)" @click="formula(1)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==1">当前时间</a-button>
                  <a-button class="margin-right-10" title="当前日期(dateFun)" @click="formula(2)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==1">当前日期</a-button>
                  <a-button class="margin-right-10" title="绝对值(absFun)" @click="formula(6)" :disabled="computationModel.notEdit||calculateSignal.stepType==1">绝对值</a-button>
                  <a-button class="margin-right-10" title="最大值(maxFun)" @click="formula(3)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">最大值</a-button>
                  <a-button class="margin-right-10" title="最小值(minFun)" @click="formula(4)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">最小值</a-button>
                  <a-button class="margin-right-10" title="平均值(averageFun)" @click="formula(5)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">平均值</a-button>
                  <a-button class="margin-right-10" title="累计值(sumFun)" @click="formula(7)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">累计值</a-button>
                  <a-button class="margin-right-10" title="绝对值(ifNullFun)" @click="formula(19)" :disabled="computationModel.notEdit">空值取0</a-button>

                  <a-button class="margin-right-10" title="取数信号符合条件的进入次数(timesFun)" @click="formula(16)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">取数信号进入次数</a-button>
                  <a-button class="margin-right-10" title="取数信号符合条件的总时间-秒(timeSecondFun)" @click="formula(33)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">取数信号合计时间</a-button>
                </div>
                <div class="height-35">
                  <a-button class="margin-right-10" title="第一个值(firstFun)" @click="formula(14)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">第一个值</a-button>
                  <a-button class="margin-right-10" title="第一个值时间(firstTimeFun)" @click="formula(17)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">第一个值时间</a-button>
                  <a-button class="margin-right-10" title="最后一个值(lastFun)" @click="formula(15)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">最后一个值</a-button>
                  <a-button class="margin-right-10" title="最后一个值时间(lastTimeFun)" @click="formula(18)" :disabled="computationModel.notEdit||computationModel.valueSource==1||calculateSignal.stepType==0">最后一个值时间</a-button>
                  <a-button class="margin-right-10" title="红黄区标准值(standardFun)" @click="formula(30)" :disabled="computationModel.notEdit||computationModel.valueSource==0||calculateSignal.stepType==1">红黄区标准值</a-button>
                  <a-button class="margin-right-10" title="跨行计算(nearFun)" @click="formula(31)" :disabled="computationModel.notEdit||computationModel.valueSource==0||calculateSignal.stepType==1">跨行计算</a-button>
                  <a-button class="margin-right-10" title="取第一个不为空的PI值(firstNotNullFun)" @click="formula(32)" :disabled="computationModel.notEdit||computationModel.valueSource==0||calculateSignal.stepType==1">取第一个不为空的PI值</a-button>
                </div>
                <div class="height-35">
                  <a-button class="margin-right-10" title="+" @click="formula(8)" :disabled="computationModel.notEdit">+</a-button>
                  <a-button class="margin-right-10" title="-" @click="formula(9)" :disabled="computationModel.notEdit">-</a-button>
                  <a-button class="margin-right-10" title="*" @click="formula(10)" :disabled="computationModel.notEdit">*</a-button>
                  <a-button class="margin-right-10" title="/" @click="formula(11)" :disabled="computationModel.notEdit">/</a-button>
                  <a-button class="margin-right-10" title="(" @click="formula(12)" :disabled="computationModel.notEdit">(</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(13)" :disabled="computationModel.notEdit">)</a-button>

                  <a-button class="margin-right-10" title=")" @click="formula(20)" :disabled="computationModel.notEdit">0</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(21)" :disabled="computationModel.notEdit">1</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(22)" :disabled="computationModel.notEdit">2</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(23)" :disabled="computationModel.notEdit">3</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(24)" :disabled="computationModel.notEdit">4</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(25)" :disabled="computationModel.notEdit">5</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(26)" :disabled="computationModel.notEdit">6</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(27)" :disabled="computationModel.notEdit">7</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(28)" :disabled="computationModel.notEdit">8</a-button>
                  <a-button class="margin-right-10" title=")" @click="formula(29)" :disabled="computationModel.notEdit">9</a-button>
                </div>
                <div class="height-35">
                  <span>字符：</span>
                  <a-input v-model:value="computationModel.character" placeholder="请输入" :size="size" class="width-200 margin-right-10" :disabled="computationModel.notEdit" allowClear/>
                  <a-button type="primary" :size="size" :disabled="computationModel.notEdit" @click="formula(100)">插入</a-button>
                </div>
                <div class="height-35">
                  <span>字段：</span>
                  <a-select v-model:value="computationModel.pointCode" show-search option-filter-prop="label" :size="size" class="width-200 margin-right-10" :options="countFieldList" placeholder="请选择" :disabled="computationModel.notEdit" allowClear></a-select>
                  <a-button type="primary" class="margin-right-10" :size="size" :disabled="computationModel.notEdit" @click="formula(99)">插入</a-button>
                  <a-button type="primary" class="margin-right-10" :size="size" @click="cleanFormula('all')">清除</a-button>
                  <a-button type="primary" class="margin-right-10" :size="size" @click="cleanFormula('refund')">退位</a-button>
                  <a-button type="primary" class="margin-right-10" :size="size" @click="complete()">补全</a-button>
                </div>
              </div>
            </div>
            <div>计算公式</div>
            <div class="text-content" style="height: 60px;">
              {{computationModel.formulaCode}}
            </div>
            <div>计算公式说明</div>
            <div class="text-content" style="height: 60px;">
              {{computationModel.formulaDescribe}}
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
    <!--**************计算模型-监控模型********************-->
    <a-row style="margin-top: 20px;" v-if="calculateSignal.stepType=='2'">
      <a-col class="gutter-row" :span="24">
        <div class="gutter-box-bottom" style="height: 336px;">
          <div class="box-piece-title">计算模型</div>
          <a-divider class="divider-style" />
          <div class="box-piece-content">
            <div style="width:100%;display: flex;justify-content: space-between;">
              <div>
                <div class="height-35">
                  <span style="padding-right: 10px;">实际值</span>
                  <a-select v-model:value="computationMonitorModel.actualValue" show-search option-filter-prop="label" :size="size" class="width-200" :options="pointArr" placeholder="请选择" allowClear></a-select>
                </div>

                <div class="height-35">
                  <span class="margin-right-10">设定值</span>
                  <a-select v-model:value="computationMonitorModel.presumePoint" show-search option-filter-prop="label" :size="size" class="width-200" :options="pointArr" placeholder="请选择" allowClear :disabled="computationMonitorModel.isRedAndYellow"></a-select>
                  <span class="margin-left-10 margin-right-10">或</span>
                  <a-input v-model:value="computationMonitorModel.presumeValue" placeholder="请输入" :size="size" class="width-80" :disabled="computationMonitorModel.isRedAndYellow"/>
                  <span class="margin-left-10 margin-right-10">或</span>
                  <a-checkbox v-model:checked="computationMonitorModel.isRedAndYellow" @change="redAndYellowChange">当前红黄区标准值</a-checkbox>
                </div>
                <div class="height-35">
                  <span style="padding-right: 10px;">是否覆盖</span>
                  <a-radio-group v-model:value="computationMonitorModel.ifCover" name="radioGroup">
                    <a-radio value="1">是</a-radio>
                    <a-radio value="0">否</a-radio>
                  </a-radio-group>
                </div>

                <div style="color:red;height: 85px;">
                  <span style="padding-right: 10px;"><ExclamationCircleOutlined />&nbsp;备注：</span><br/>
                  <span style="padding-right: 10px;">差：实际值-设定值</span><br/>
                  <span style="padding-right: 10px;">比值：(实际值-设定值)/设定值</span><br/>
                  <span style="padding-right: 10px;">偏差值：差的绝对值</span>
                </div>
              </div>
              <div style="width:60%">
                <div style="display: flex;justify-content: flex-start;align-items: center;" class="height-35">
                  取数规则：
                </div>
                <div style="display: flex;justify-content: flex-start;align-items: center;" class="height-35">
                  <span>当&nbsp;</span>
                  <a-input v-model:value="computationMonitorModel.leftValue" placeholder="请输入" :size="size" class="width-90" allowClear/>
                  <a-select v-model:value="computationMonitorModel.leftCondition" :size="size" class="width-90" :options="fetchConditionArr" placeholder="请选择" allowClear></a-select>
                  <a-select v-model:value="computationMonitorModel.valueType" :size="size" class="width-90" :options="priceTypeArr" placeholder="请选择" allowClear @change="redAndYellowValChange"></a-select>
                  <a-select v-model:value="computationMonitorModel.rightCondition" :size="size" class="width-150" :options="fetchRedYellowAllConditionArr" placeholder="请选择" allowClear></a-select>
                  <a-input v-model:value="computationMonitorModel.rightValue" placeholder="请输入" :size="size" class="width-90" allowClear v-if="isRYShow()"/>
                  
                </div>
                <div style="display: flex;justify-content: flex-start;align-items: center;" class="height-35">
                  <span>的&nbsp;</span>
                  <a-select v-model:value="computationMonitorModel.calculationMethod" :size="size" class="width-100" :options="calculationMethodArr" placeholder="请选择" allowClear></a-select>
                  <a-select v-model:value="computationMonitorModel.calculationCondition" :size="size" class="width-90" :options="fetchConditionArr" placeholder="请选择" allowClear></a-select>
                  <a-select v-model:value="computationMonitorModel.valueCondition" :size="size" class="width-100" :options="valueConditiondArr" placeholder="请选择" allowClear></a-select>
                  <a-input v-model:value="computationMonitorModel.calculationNumber" placeholder="请输入" :size="size" class="width-90" v-if="computationMonitorModel.valueCondition !='-1'" allowClear/>
                  <span>，&nbsp;</span>
                </div>
                <div style="display: flex;justify-content: flex-start;align-items: center;" class="height-35">
                  <span v-if="computationMonitorModel.valueCondition !='-1'">次</span>
                  <span>取&nbsp;</span>
                  <a-select v-model:value="computationMonitorModel.resultType" :size="size" class="width-100" :options="priceTypeArr" placeholder="请选择" allowClear @change="redAndYellowValChange"></a-select>
                  <a-select v-model:value="computationMonitorModel.resultModel" :size="size" class="width-170" :options="resultModelArr" placeholder="请选择" allowClear></a-select>
                  <DownCircleOutlined class="width-50 padding-left-10 icon-style" title="添加" @click="addMonitorCount"/>
                </div>
              </div>
            </div>
            <!-- <div>计算公式</div>
            <div class="text-content" style="height: 60px;">
              {{computationMonitorModel.formulaCode}}
            </div> -->
            <div>计算公式说明</div>
            <div class="text-content" style="height: 72px;">
              <div v-for="(item, index) in computationMonitorModelList" :key="index">{{index+1}}.{{item.formulaDescribe}} <span class="del-signal" @click="delMonitorCount(index)" style="cursor: pointer;color:red" title="删除"><CloseCircleOutlined/></span></div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted, watch } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { Form } from 'ant-design-vue';
  import {getPoint,getFieldByTableId,saveOrUpdate,pointStep,algorithmData,getDictionary} from '../PiAlgorithm.api';
  import { getResultModel } from '/@/views/dataCalculation/pi/transformationModel/PiResultTransformationModel.api';
  import { DownCircleOutlined,PlusCircleOutlined,ExclamationCircleOutlined,CloseCircleOutlined } from '@ant-design/icons-vue';

  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();


  const props = defineProps({
      formDisabled: { type: Boolean, default: false },
      formData: { type: Object, default: () => {} },
      formBpm: { type: Boolean, default: true },
      isAdd: { type: Boolean, default: true },
  });

  const formRef = ref();
  const size = ref('middle');
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok','close']);
  const formData = reactive<Record<string, any>>({
      id: '',
  });
  //点位-类型为步骤
  const pointStepArr = reactive<Record<string, any>>([]);
  //点位-全量
  const pointArr = reactive<Record<string, any>>([]);
  //点位-非步骤
  const pointNotStepArr = reactive<Record<string, any>>([]);
  //步骤集合(点位(步骤类型)关联设备,设备获取步骤)
  const stepArr = reactive<Record<string, any>>([]);
  //是否显示步骤集合isStep
  const isStep = ref<boolean>(false);

  //计算信号
  const calculateSignal = reactive<Record<string, any>>({
    id: '',
    stepType:'0',
    advanceTime:0,
    pointType: 0,
  });
  //取数信号-待写入
  const retrievalSignal = reactive<Record<string, any>>([]);
  //取数信号-历史写入(展示)
  const retrievalSignalHistory = reactive<Record<string, any>>([]);
  //取数信号-历史写入(存储)
  const retrievalSignalData = reactive<Record<string, any>>([]);
  //取数信号-适用于
  const applyTo = ref('0');
  //取数信号-是否显示特殊(true 显示所有, false显示没隐藏)
  const isDisplay = ref(false);
  //取数信号-时间类型
  const timeTypeArr = reactive<Record<string, any>>([
    { value: 0, label: '整点小时' },
    { value: 1, label: '偶数小时' },
    { value: 2, label: '指定时间' },
  ]);

  //取数步骤-开始
  const retrievalStepStart = reactive<Record<string, any>>({
      id: '',
      stepType: '0',
      advanceTime:0,
      frequency: '',
      isSlicing: '',
  });
  //取数步骤-结束
  const retrievalStepEnd = reactive<Record<string, any>>({
      id: '',
      stepType: '1',
      advanceTime:0
  });
  //取数步骤-是否切割
  const isSlicingArr = reactive<Record<string, any>>([
    { value: '1', label: '是' },
    { value: '0', label: '否' }
  ]);
  //取数步骤-PI字段-类型为时间
  const fieldDateTimeList = reactive<Record<string, any>>([]);

  //取数信号-PI日报字段(全量)
  const piFieldAllList = reactive<Record<string, any>>([]);

  //唯一约束
  const uniqueList = reactive<Record<string, any>>([]);
  //计算模型-字段
  const countFieldList = reactive<Record<string, any>>([]);
  //计算模型-PI日报字段
  const dailyFieldList = reactive<Record<string, any>>([]);
  //计算模型-汉化代码
  const dictOptions = reactive<Record<string, any>>([]);
  //计算模型-监控取值
  const computationMonitorModelList = reactive<Record<string, any>>([]);
  //计算模型-显示控制
  const computationShow = reactive<Record<string, any>>({
    sortField: false,
  });
  //计算模型-结果模型转换
  const resultModelList = reactive<Record<string, any>>([]);

  //计算模型
  const computationModel = reactive<Record<string, any>>({
    id: '',
    valueSource: '0',
    ifCover: '1',
    advanceTime:0,
    sortMode:'DESC', //默认倒序
  });
  //计算模型-特殊字段
  const computationSpecialModel = reactive<Record<string, any>>({
    filterField:[]
  })
  
  //计算模型-监控取值
  const computationMonitorModel = reactive<Record<string, any>>({
      id: '',
      presumeValue: '',
      ifCover: '1',
  });


  const modelData = reactive<Record<string, any>>({
      // isForm: true,
      isList: true,
      isSort: false,
      sortType: true,
      isQuery: false
  });

  //提前或延后
  const advanceOrDelayArr = reactive<Record<string, any>>([
    { value: '0', label: '提前' },
    { value: '1', label: '延后' }
  ]);
  //提前
  const advanceArr = reactive<Record<string, any>>([
    { value: '0', label: '提前' },
  ]);
  //延后
  const delayArr = reactive<Record<string, any>>([
    { value: '1', label: '延后' }
  ]);
  //取数信号-提前/延后
  const computationAdvanceArr = reactive<Record<string, any>>([]); 

  //取数步骤-条件
  const conditionArr = reactive<Record<string, any>>([
    { value: '1', label: '>' },
    { value: '2', label: '≥' },
    { value: '3', label: '=' },
    { value: '4', label: '≠' },
    { value: '5', label: '<' },
    { value: '6', label: '≤' },
    { value: '7', label: '进入' },
    { value: '8', label: '结束' }
  ]);
  //取数步骤-值类型
  const stepValueTypeArr = reactive<Record<string, any>>([
    { value: '1', label: '触发时间' },
    { value: '2', label: '步骤号' },
    { value: '3', label: 'PI' }
  ]);

  //计算信号-条件
  const countConditionArr = reactive<Record<string, any>>([
    { value: '7', label: '进入' },
    { value: '8', label: '结束' }
  ]);

  //取数信号-条件
  const fetchConditionArr = reactive<Record<string, any>>([
    { value: '1', label: '>' },
    { value: '2', label: '≥' },
    { value: '3', label: '=' },
    { value: '4', label: '≠' },
    { value: '5', label: '<' },
    { value: '6', label: '≤' },
  ]);

  //取数信号-条件
   const fetchDictConditionArr = reactive<Record<string, any>>([
    { value: '7', label: '包含' },
  ]);

  //监控取数-取数规则(红黄区)
  const fetchRedYellowConditionArr = reactive<Record<string, any>>([
    { value: '7', label: '进入红区' },
    { value: '8', label: '进入黄区' },
    { value: '9', label: '进入绿区' },
    { value: '10', label: '进入红黄区' }
  ]); 
  //监控取数-取数规则(红黄区)
  const fetchRedYellowAllConditionArr = reactive<Record<string, any>>([]); 

  //取数信号-条件(中文)
  const fetchConditionCnArr = reactive<Record<string, any>>([
    { value: '1', label: '大于' },
    { value: '2', label: '大于等于' },
    { value: '3', label: '等于' },
    { value: '4', label: '不等于' },
    { value: '5', label: '小于' },
    { value: '6', label: '小于等于' },
  ]);

   //结果类型
   const resultTypeArr = reactive<Record<string, any>>([
      { value: '', label: '请选择' },
      { value: '1', label: '天' },
      { value: '2', label: '小时' },
      { value: '3', label: '分钟' },
      { value: '4', label: '秒' }
   ]);
   //当前年/当前小时
    const yearHourArr = reactive<Record<string, any>>([
      { value: '', label: '请选择' },
      { value: 'yearFun', label: '当前年(4位)' },
      { value: 'year2Fun', label: '当前年(2位)' },
      { value: 'hourFun', label: '当前小时' },
      { value: 'hour2Fun', label: '当前小时(偶数)' },
      { value: 'minuteFun', label: '当前分钟' },
      { value: 'minFreFun', label: '自定义分钟频率' },
      { value: 'timeFreFun', label: '自定义时间' },
      { value: 'currentDateFun', label: '当前日期' },
      { value: 'yesterdayDateFun', label: '昨天日期' }
   ]);
   //自定义分支频率 2 3 4 5 6 10 12 15 20 30 
   const minFreArr = reactive<Record<string, any>>([
      { value: '2', label: '2分钟' },
      { value: '3', label: '3分钟' },
      { value: '4', label: '4分钟' },
      { value: '5', label: '5分钟' },
      { value: '6', label: '6分钟' },
      { value: '10', label: '10分钟' },
      { value: '12', label: '12分钟' },
      { value: '15', label: '15分钟' },
      { value: '20', label: '20分钟' },
      { value: '30', label: '30分钟' }
   ]);

    //计算模型-监控取值-计算值类型
    const priceTypeArr = reactive<Record<string, any>>([
      { value: '', label: '请选择' },
      { value: '1', label: '实际值' },
      { value: '2', label: '差' },
      { value: '3', label: '比值' },
      { value: '4', label: '偏差值' },
   ]);

  //计算模型-范围取值-字段排序
  const sortModeList = reactive<Record<string, any>>([
    { value: '', label: '请选择' },
    { value: 'ASC', label: '正序' },
    { value: 'DESC', label: '倒序' }
  ]);
   
   //计算模型-监控取值-计算方式
   const calculationMethodArr = reactive<Record<string, any>>([
      { value: '', label: '请选择' },
      { value: '1', label: '连续次数' },
      { value: '2', label: '次数' }
   ]);
   //计算模型-监控取值-取值
   const resultModelArr = reactive<Record<string, any>>([
      { value: '', label: '请选择' },
      { value: '1', label: '最大值' },
      { value: '2', label: '最小值' },
      { value: '3', label: '平均值' },
      { value: '4', label: '第一个值' },
      { value: '5', label: '最后一个值' },
      { value: '6', label: '最大偏差实际值' },
      { value: '7', label: '最小偏差实际值' },
      { value: '8', label: '最大偏差时间' },
      { value: '9', label: '最小偏差时间' },
      { value: '10', label: '第一个值时间' },
      { value: '11', label: '最后一个值时间' },
      { value: '17', label: '符合条件第一个值' },
      { value: '18', label: '符合条件第一个值时间' },
      { value: '19', label: '符合条件最后一个值' },
      { value: '20', label: '符合条件最后一个值时间' }

      // { value: '6', label: '偏差最大的值' },
   ]);
  //监控取数-取值(红黄区)
  const resultModelRedYellowArr = reactive<Record<string, any>>([
    { value: '12', label: '第一个绿区值' },
    { value: '13', label: '最后一个绿区值' },
    { value: '14', label: '绿区平均值' },
    { value: '15', label: '黄区偏离最大实际值' },
    { value: '16', label: '红区偏离最大实际值' }
  ]); 

  //计算模型-监控取值-取值条件
  const valueConditiondArr = reactive<Record<string, any>>([
    { value: '', label: '请选择' },
    { value: '-1', label: '全部' },
    { value: '0', label: '自定义' }
  ]);
  //记录操作记录
  const formulaHistory = reactive<Record<string, any>>([]);
  const formulaCnHistory = reactive<Record<string, any>>([]);

  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = {
      name: [{ required: true, message: `字段中文名称不为空!` }],
      type: [{ required: true, message: `字段类型不为空!` }],
  };
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: true });

  // 表单禁用
  const disabled = computed(() => {
  if (props.formBpm === true) {
      if (props.formData.disabled === false) {
      return false;
      } else {
      return true;
      }
  }
  return props.formDisabled;
  });
  const setEditStatus = computed(() => {
  if (!props.isAdd) {
      return true;
  }
  return false;
  });


  //计算信号
  function stepTypeChange(){
    if(calculateSignal.stepType =='0'){
      retrievalStepStart.frequency = ''
      retrievalStepStart.isSlicing = ''
    }else{
      retrievalStepStart.frequency = 1
      retrievalStepStart.isSlicing = '1'
      computationModel.valueSource = '0'
    }
  }
  //取数信号
  function radioGroupChange(val: string){
    if(calculateSignal.stepType == '2'){
      fetchRedYellowAllConditionArr.length = 0;
      fetchConditionArr.forEach((obj) => {
        fetchRedYellowAllConditionArr.push(obj);
      });
    }
    computationAdvanceArr.length = 0
    if(applyTo.value == '0'){
      //计算信号 提前
      advanceArr.forEach((obj) => {
        computationAdvanceArr.push(obj);
      });
    }else{
      //取数步骤 延后
       delayArr.forEach((obj) => {
        computationAdvanceArr.push(obj);
      });
    }

    //清空选中的数据
    retrievalSignal.forEach(item => {
      // 仅当属性存在时才清空，避免添加不必要的属性
      if (item.hasOwnProperty('advanceDelay')) {
        item.advanceDelay = undefined; // 清空为字符串空值
      }
    });
  }

  //获取点位(步骤)
  async function getPointData(){
    let res = await getPoint({});
    let newArr = res.map(item => ({
        value: item.pointCode,
        label: item.pointName+"("+item.pointCode+")"
    }));
    //所有点位
    pointArr.push(...newArr);
    //点位为步骤/为非步骤
    res.forEach((item) => {
      if (item.pointType == '1') {
        //步骤
        pointStepArr.push({
          value: item.pointCode,
          label: item.pointName+"("+item.pointCode+")"
        });
      }else{
        //非步骤
        pointNotStepArr.push({
          value: item.pointCode,
          label: item.pointName+"("+item.pointCode+")"
        });
      }
    });

    //计算模型-基础点位-字段初始化
    if(computationModel.valueSource != '1'){
      pointArr.forEach((element, index) => {
        countFieldList[index] = element;
      });
    }

  }

  //获取当前表对应的字段(唯一约束) 
  async function getUniqueData(val){
    // let newArr = res.map(item => {
    //   if(item.name){
    //     return{
    //       colName:item.name,
    //       colCode:item.fieldName
    //     }
    //   }else{
    //     return null
    //   }
    //     //tagCode:item.fieldName
    // });
    // uniqueList.push(...newArr);
    //已配置的唯一约束不做处理
    let newArr = []
    if(val && val.length > 0){
      newArr = val;
    }else{
      let res = await getFieldByTableId({tableId: calculateSignal.piTableId,isUnique:1});
      newArr = checkSameNames(res,val);
    }
    uniqueList.push(...newArr);
    // uniqueJson.forEach((element, index) => {
    //     if(element.tagValue === 'yearFun' || element.tagValue === 'hourFun' || element.tagValue === 'hour2Fun' || element.tagValue === 'year2Fun' || element.tagValue === 'minuteFun'){
    //       element.yearHour = element.tagValue
    //     }else if(element.tagValue){
    //       element.uniqueValue = element.tagValue
    //     }
    //   });
  }

  //合并
  function checkSameNames(a, b) {
    let c = [];
    for (let i = 0; i < a.length; i++) {
        let found = false;
        for (let j = 0; j < b.length; j++) {
            if (a[i].fieldName === b[j].colCode) {
                c.push(b[j]);
                found = true;
                break;
            }
        }
        if (!found) {
          let obj = {
            colName:a[i].name,
            colCode:a[i].fieldName
          }
          c.push(obj);
        }
    }
    return c;
  }


  //计算模型-PI日报(全量字段)
  async function getDailyFieldData(){
    let res = await getFieldByTableId({tableId: calculateSignal.piTableId});
    //全量
    let allArr = res.map(item => {
      return { 
        label:item.name,
        value:item.fieldName}
    }).filter(Boolean);
    //排除日期
    let newArr = res.map(item => {
      if(item.type != '4'){
        return { 
          label:item.name,
          value:item.fieldName}
        }
    }).filter(Boolean);

    //类型为时间
    let timeArr = res.map(item => {
      if(item.type == '6'){
        return { 
          label:item.name,
          value:item.fieldName}
        }
    }).filter(Boolean);
    dailyFieldList.push(...newArr);
    fieldDateTimeList.push(...timeArr);
    piFieldAllList.push(...allArr);

    //判断历史唯一约束配置在PI中是否存在 uniqueList
   const hasInvalidItems = uniqueList.some(uniqueItem => {
     return !allArr.some(item => item.value === uniqueItem.colCode);
   });
   if(hasInvalidItems){
     createMessage.warning("存在无效的唯一约束配置，请先删除！");
   }

  }

  //计算模型-汉化模型
  async function getDictionaryData(){
    let res = await getDictionary({});
    let newArr = res.map(item => ({
        label:item.label,
        value:item.value
    }));
    dictOptions.push(...newArr);
  }

  //计算模型-监控取值-数据规则
  function addMonitorCount(){  
    
    //取数规则
    let leftValue = computationMonitorModel.leftValue;
    let leftCondition = computationMonitorModel.leftCondition;
    let valueType = computationMonitorModel.valueType;
    let rightCondition = computationMonitorModel.rightCondition;
    let rightValue = computationMonitorModel.rightValue;
    let calculationMethod = computationMonitorModel.calculationMethod;
    let calculationCondition = computationMonitorModel.calculationCondition;
    let calculationNumber = computationMonitorModel.calculationNumber;
    let resultType = computationMonitorModel.resultType;
    let resultModel = computationMonitorModel.resultModel;
    //取值条件[-1全部/0自定义]
    if(computationMonitorModel.valueCondition === '-1'){
      computationMonitorModel.calculationNumber = '-1'
      calculationNumber = '-1'
    }

    //添加到集合
    let newObject = Object.assign({}, computationMonitorModel);

    //红黄区computationMonitorModel.rightValue
    let targetChars = ['7', '8', '9', '10'];
    //验证是否为空
    if(((leftValue && leftCondition) || (rightCondition && (targetChars.includes(rightCondition) || rightValue))) && valueType && calculationMethod && calculationCondition && calculationNumber && resultType && resultModel){
      let leftConditionMs = getValueByKey(fetchConditionArr,leftCondition)
      let valueTypeMs = getValueByKey(priceTypeArr,valueType)
      let rightConditionMs = getValueByKey(fetchRedYellowAllConditionArr,rightCondition)
      let calculationMethodMs = getValueByKey(calculationMethodArr,calculationMethod)
      let calculationConditionMs = getValueByKey(fetchConditionArr,calculationCondition)
      let resultTypeMs = getValueByKey(priceTypeArr,resultType);

      //取值条件[-1全部/0自定义]
      let calculationNumberMs = ""
      if(computationMonitorModel.calculationNumber != '-1'){
        calculationNumberMs = computationMonitorModel.calculationNumber+"次"
      }else{
        calculationNumberMs = "全部"
      }
      
      let resultModelMs = getValueByKey(resultModelArr,resultModel)
      //生成公式描述
      if(!leftValue){
        leftValue=""
      }
      if(!leftConditionMs){
        leftConditionMs=""
      }
      if(!rightConditionMs){
        rightConditionMs=""
      }
      if(!rightValue){
        rightValue=""
      }
      let formulaDescribe = "当"+leftValue+leftConditionMs+valueTypeMs+rightConditionMs+rightValue+"的"+calculationMethodMs+calculationConditionMs+calculationNumberMs+"，取"+resultTypeMs+resultModelMs;
      newObject.formulaDescribe = formulaDescribe;
      computationMonitorModelList.push(newObject);
      //清空
      cleanMonitorCount();
    }else{
      //
      createMessage.warning("取数规则填写不完整!");
    }
    //红黄区清空
    redAndYellowValChange();
  }
  //计算模型-监控取值-清空取数规则
  function cleanMonitorCount(){
    computationMonitorModel.leftValue = "";
    computationMonitorModel.leftCondition = "";
    computationMonitorModel.valueType = "";
    computationMonitorModel.rightCondition = "";
    computationMonitorModel.rightValue = "";
    computationMonitorModel.calculationMethod = "";
    computationMonitorModel.calculationCondition = "";
    computationMonitorModel.calculationNumber = "";
    computationMonitorModel.resultType = "";
    computationMonitorModel.resultModel = "";
    //computationMonitorModel.isRedAndYellow = false;
  }

  //计算模型-监控取值-删除计算公式说明
  function delMonitorCount(index){
    computationMonitorModelList.splice(index, 1);
  }

  //计算模型-监控取值-当前红黄区标准值/取数规则-条件/取数规则-取值
  function redAndYellowChange(index){
    if(computationMonitorModel.isRedAndYellow){
      computationMonitorModel.presumeValue ="";
      computationMonitorModel.presumePoint ="";
    }
    //改变取数规则下拉
    redAndYellowValChange()

  }
  //计算模型-监控取值-取数规则(条件/取值)
  function redAndYellowValChange(){
    //条件
    if(computationMonitorModel.isRedAndYellow && computationMonitorModel.valueType == '1'){
      //当前红黄区标准值 且 条件为实际值
      fetchRedYellowConditionArr.forEach((obj) => {
        fetchRedYellowAllConditionArr.push(obj);
      });
    }else{
      //移除红区相关选项
      let targetChars = ['7', '8', '9', '10'];
      if(targetChars.includes(computationMonitorModel.rightCondition)){
        computationMonitorModel.rightCondition = "";
      }
      //移除选项
      fetchRedYellowAllConditionArr.length = 0;
      fetchConditionArr.forEach((obj) => {
        fetchRedYellowAllConditionArr.push(obj);
      });
    }

    //取值
    if(computationMonitorModel.isRedAndYellow && computationMonitorModel.resultType == '1'){
      //当前红黄区标准值 且 取值为实际值
      resultModelRedYellowArr.forEach((obj) => {
        resultModelArr.push(obj);
      });
    }else{
      //移除取值红黄区相关
      let targetChars = ['12', '13'];
      if(targetChars.includes(computationMonitorModel.resultModel)){
        computationMonitorModel.resultModel = "";
      }
      //移除选项(绿区)
      const newArray = resultModelArr.filter(item => item.value!== '12' && item.value!== '13' && item.value!== '14' && item.value!== '15' 
        && item.value!== '16');
      resultModelArr.length = 0
      newArray.forEach((obj) => {
        resultModelArr.push(obj);
      });

    }
  }
  
  //计算模型-监控取值-取数规则-红黄区
  function isRYShow(){
    let targetChars = ['7', '8', '9', '10'];
    return !targetChars.includes(computationMonitorModel.rightCondition)
  }


  onMounted(() => {
    //getDeviceTypeArr();
    //初始化取数信号
    initRetrievalSignal();
    //获取点位
    // getPointData();
    //获取唯一约束
    //getUniqueData();
  });

  //初始化取数信号
  function initRetrievalSignal(){
    retrievalSignal.value=[];
    let obj = {"id":"","pointCode":undefined,"calculateType":undefined,"calculateValue":"","advanceDelay":undefined,"advanceTime":"","valueType":"0","display":"1"};
    retrievalSignal.push(obj);
    //取数信号-条件
    fetchConditionChange(retrievalSignal[0]);
  }
  //追加取数信号
  function addRetrievalSignal(){
    let obj = {"id":"","pointCode":undefined,"calculateType":undefined,"calculateValue":"","advanceDelay":undefined,"advanceTime":"","valueType":"0","display":"1"};
    retrievalSignal.push(obj);
    //取数信号-条件
    fetchConditionChange(retrievalSignal[retrievalSignal.length-1]);
  }
  //写入记录
  function addRetrievalSignalData(){
    //为空判断
    //let str = "";
    let strArr = [];
    let dataArr = [];
    retrievalSignal.forEach((e, index, array) => {
      if(e.pointCode && e.calculateType && e.calculateValue){
        let pointName = getValueByKey(pointArr,e.pointCode)
        let explanation = "";
        let calculateType = getValueByKey(e.fetchDynamicConditionArr,e.calculateType)
        let advanceDelay = getValueByKey(advanceOrDelayArr,e.advanceDelay);
        //判断取值类型
        let calculateValue = e.calculateValue
        if(e.valueType == '1'){
          //点位
          calculateValue = getValueByKey(pointArr,calculateValue);
        }else if(e.valueType == '2'){
          //字典
          calculateValue = getValueByKey(dictOptions,calculateValue);
        }
        let advanceTime = e.advanceTime?e.advanceTime+"秒":'';
        advanceDelay = advanceDelay?advanceDelay:'';
        if(strArr && strArr.length > 0){
          let str = " 且 "+ pointName + calculateType + calculateValue +" "+ advanceDelay + advanceTime;
          strArr.push({"display":e.display, "str":str});
          explanation = " 且 "+ pointName + calculateType + calculateValue +" "+ advanceDelay + advanceTime;
        }else{
          //let calculateType = getValueByKey(fetchConditionArr,e.calculateType)
          let str = pointName + calculateType + calculateValue +" "+ advanceDelay + advanceTime;
          strArr.push({"display":e.display, "str":str});
          explanation = pointName + calculateType + calculateValue +" "+ advanceDelay + advanceTime;
        }
        let obj={
          "pointCode": e.pointCode,
          "pointName": pointName,
          "calculateType": e.calculateType,
          "calculateValue": e.calculateValue,
          "explanation": explanation,
          "applyTo": applyTo.value,
          "advanceDelay":e.advanceDelay,
          "advanceTime":e.advanceTime,
          "valueType":e.valueType,
          "display": e.display
        }
        dataArr.push(obj);
        //取数信号-条件
        fetchConditionChange(dataArr[0]);
      }
    });

    if(strArr && strArr.length > 0){
      //适用于
      let applyToStr = "计算信号";
      if(applyTo.value == '1'){
        applyToStr = "取数步骤"
      }
      retrievalSignalData.push(dataArr);
      // retrievalSignalHistory.push(applyToStr+": "+str);
      retrievalSignalHistory.push({applyToStr, strArr});
      //初始化写入
      retrievalSignal.length = 0;
      initRetrievalSignal();
    }else{
      createMessage.warning("请配置点位!");
    }
  }
  //删除历史
  function delRetrievalSignal(index){
    retrievalSignalHistory.splice(index, 1);
    retrievalSignalData.splice(index, 1);
  }
/**
 * 新增
 */
function add(record) {
  edit(record);
}

//传入数组对象 获取指定key对应的值
function getValueByKey(array,key) {
    let result;
    array.forEach((obj) => {
        if (obj["value"] === key) {
            result = obj["label"];
        }
    });
    return result;
}

/**
 * 编辑
 */
function edit(record) {
  nextTick(async () => {
    resetFields();
    await getPointData();
    //赋值
    //Object.assign(formData, record);
    calculateSignal.piTableId = record.piTableId;
    calculateSignal.piTable = record.piTable;
    calculateSignal.piCode = record.piCode;
    calculateSignal.piName = record.piName;
    console.log('获取表单数据', formData);
    //值回显
    if(record.id){
      if(record.displayType ==='2'){
        isDisplay.value = true;
      }

      Object.assign(calculateSignal, record);
      //获取其他回显数据
      getAlgorithmData(record.id);
      //uniqueList.value = JSON.parse(calculateSignal.uniqueJson)

      let uniqueJson = JSON.parse(calculateSignal.uniqueJson);

      //唯一约束-时间
      uniqueJson.forEach((element, index) => {
        if(element.tagValue === 'yearFun' || element.tagValue === 'hourFun' || element.tagValue === 'hour2Fun' || element.tagValue === 'year2Fun' 
          || element.tagValue === 'minuteFun' || element.tagValue === 'minFreFun' || element.tagValue === 'timeFreFun' || element.tagValue === 'currentDateFun'
        || element.tagValue === 'yesterdayDateFun'){
          element.yearHour = element.tagValue
        }else if(element.tagValue){
          element.uniqueValue = element.tagValue
        }
      });
      //uniqueList.push(...uniqueJson);
      getUniqueData(uniqueJson);
    }else{
      //获取唯一约束
      getUniqueData([]);
    }
    //获取pi日报字段
    getDailyFieldData();
    //获取汉化代码
    getDictionaryData();
    //红黄区(下拉)
    fetchConditionArr.forEach((obj) => {
      fetchRedYellowAllConditionArr.push(obj);
    });
    //取数信号-计算信号(默认值)
    advanceArr.forEach((obj) => {
      computationAdvanceArr.push(obj);
    });
    //计算模型-结果模型转换
    getResultModelData();
  });
}
//数据回显
async function getAlgorithmData(id){
    let res = await algorithmData({id});
    

    if(res.computationModel){
       Object.assign(computationModel, res.computationModel);
       computationModel.valueSource = res.computationModel.valueSource+""
      //判断取值来源是否是PI日报
      if(computationModel.valueSource === '1'){
        countFieldList.length = 0
        dailyFieldList.forEach((element, index) => {
          countFieldList[index] = element;
        });
      }
      //判断是否包含[跨行计算]
      if(computationModel.formulaCode && computationModel.formulaCode.includes("nearFun(")){
        computationShow.sortField = true;
      }else{
        computationShow.sortField = false;
      }
      //过滤字段
      if(computationModel.filterField){
        computationSpecialModel.filterField = computationModel.filterField.split(",")
      }
      

    }
    //取数信号
    if(res.retrievalSignal){
      let retrievalSignal = JSON.parse(res.retrievalSignal)
      retrievalSignalData.push(...retrievalSignal);
      retrievalSignal.forEach((arr, index, array) => {
       // let str = "";
        let strArr = [];
        arr.forEach((e, index, array) => {
          //str+=e.explanation;
          debugger
          if(!isDisplay.value && e.display === '1'){
            let str = e.explanation;
            if(strArr && strArr.length == 0 && str.startsWith(' 且 ')){
              str = str.replace(' 且 ', '');
            }
            strArr.push({"display":e.display, "str": str});
          }else if(isDisplay.value){
            strArr.push({"display":e.display, "str": e.explanation});
          }
          
        })
        if(strArr && strArr.length > 0){
          //适用于 计算信号/取数步骤
          let applyToStr = "";
          if(arr[0].applyTo ){
            if(arr[0].applyTo == '1'){
              applyToStr = "取数步骤: ";
            }else{
              applyToStr = "计算信号: ";
            }
          }
          // retrievalSignalHistory.push(applyToStr+str);
          retrievalSignalHistory.push({applyToStr, strArr});
        }
      })
      
    }
    //计算模型-监控取数
    if(res.computationMonitorModelList && res.computationMonitorModelList.length > 0){
      computationMonitorModel.actualValue = res.computationMonitorModelList[0].actualValue;
      computationMonitorModel.presumePoint = res.computationMonitorModelList[0].presumePoint;
      computationMonitorModel.presumeValue = res.computationMonitorModelList[0].presumeValue;
      computationMonitorModel.ifCover = res.computationMonitorModelList[0].ifCover;

      //当前红黄区标准值 特殊处理
      if(res.computationMonitorModelList[0].presumeValue == 'piStandardData'){
        computationMonitorModel.isRedAndYellow = true
        computationMonitorModel.presumeValue = '';
      }
      computationMonitorModelList.push(...res.computationMonitorModelList)
    }

    //取数步骤
    if(res.retrievalStepStart){
      Object.assign(retrievalStepStart, res.retrievalStepStart);
      setStartStep(retrievalStepStart.stepValueType);
    }
    if(res.retrievalStepEnd){
      Object.assign(retrievalStepEnd, res.retrievalStepEnd);
      setEndStep(retrievalStepEnd.stepValueType);
    }
};



  /**
   * 提交数据
   */
  async function submitForm() {
      // 触发表单验证
      await validate();
      confirmLoading.value = true;
      const isUpdate = ref<boolean>(false);
      let formAllData = reactive<Record<string, any>>({});
      //计算信号calculateSignal
      formAllData.calculateSignal = calculateSignal;
      //取数信号(多个值)
      //retrievalSignalData
      formAllData.retrievalSignal = JSON.stringify(retrievalSignalData);

      //取数步骤
      formAllData.retrievalStepStart = retrievalStepStart; //开始
      formAllData.retrievalStepEnd = retrievalStepEnd; //结束

      
      //唯一约束 json
      let uniqueArrTemp = [] as any;
      //唯一约束验证
      let uniqueVerifyArr = [] as any;
      uniqueList.forEach((element, index) => {
        let tagValue = "";
        //let frequency = "";
        if(element.uniqueValue){
          tagValue = element.uniqueValue;
        }else if(element.yearHour){
          tagValue = element.yearHour;
        }
        uniqueArrTemp.push(
          {
            "colName":element.colName||'',
            "colCode":element.colCode||'',
            "tagCode":element.tagCode||'',
            "tagValue":tagValue||'',
            "frequency": element.frequency||'',
            "monitorModelId": element.monitorModelId||'',
          })
        //判断是否为"自定义分钟频率"/"自定义时间"
        if((tagValue==='minFreFun' || tagValue === 'timeFreFun') && !element.frequency){
          uniqueVerifyArr.push("唯一约束["+element.colName+"]"+(tagValue==='minFreFun'?"自定义分钟频率不为空":"自定义时间不为空"));
        }
      });
      //唯一约束验证
      if(uniqueVerifyArr && uniqueVerifyArr.length > 0){
        createMessage.warning(uniqueVerifyArr[0]);
        confirmLoading.value = false;
        return false;
      }

      
      formAllData.calculateSignal.uniqueJson = JSON.stringify(uniqueArrTemp);
      //过滤字段
      if(computationSpecialModel.filterField && computationSpecialModel.filterField.length > 0){
        computationModel.filterField = computationSpecialModel.filterField.join(",")
      }
      //计算模型(computationModel)
      formAllData.computationModel = computationModel;

      //监控数据
      if(calculateSignal.stepType=='2'){
        if(!(computationMonitorModel.isRedAndYellow || computationMonitorModel.presumeValue || computationMonitorModel.presumePoint)){
          createMessage.warning("设定值不为空");
          confirmLoading.value = false;
          return false;
        }
        if(computationMonitorModelList.length == 0){
          createMessage.warning("计算公式不为空");
          confirmLoading.value = false;
          return false;
        }

        //更新实际和设定值
        for(let i=0; i<computationMonitorModelList.length; i++){
          computationMonitorModelList[i].actualValue = computationMonitorModel.actualValue
          computationMonitorModelList[i].presumePoint = computationMonitorModel.presumePoint
          computationMonitorModelList[i].presumeValue = computationMonitorModel.presumeValue
          computationMonitorModelList[i].ifCover = computationMonitorModel.ifCover
          //当前红黄区标准值 特殊处理
          if(computationMonitorModel.isRedAndYellow){
            computationMonitorModelList[i].presumeValue = 'piStandardData'
          }
        }
        formAllData.computationMonitorModelList = computationMonitorModelList;
      }

      await saveOrUpdate(formAllData, isUpdate.value)
      .then((res) => {
      if (res.success) {
          // const successMessage = res.message ? res.message : t('language.save');
          createMessage.success(res.message);
          emit('ok', calculateSignal.id);
      } else {
          createMessage.warning(res.message);
      }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }


    //生成公式
    function formula(type){
      if(!computationModel.formulaCode){
        computationModel.formulaCode=""
      }
      if(!computationModel.formulaDescribe){
        computationModel.formulaDescribe=""
      }
      if(type === 1){
        computationModel.formulaCode ="timeFun()"
        computationModel.formulaDescribe = "当前时间"
        formulaHistory.push("timeFun()");
        formulaCnHistory.push("当前时间");
        //选当前时间不可编辑
        computationModel.notEdit=true;
      }else if(type === 2){
        computationModel.formulaCode ="dateFun()"
        computationModel.formulaDescribe = "当前日期"
        formulaHistory.push("dateFun()");
        formulaCnHistory.push("当前日期");
         //选当前日期不可编辑
        computationModel.notEdit=true;
      }else if(type === 3){
        computationModel.formulaCode+="maxFun("
        computationModel.formulaDescribe+="最大值("
        formulaHistory.push("maxFun(");
        formulaCnHistory.push("最大值(");
      }else if(type === 4){
        computationModel.formulaCode+="minFun("
        computationModel.formulaDescribe+="最小值("
        formulaHistory.push("minFun(");
        formulaCnHistory.push("最小值(");
      }else if(type === 5){
        computationModel.formulaCode+="averageFun("
        computationModel.formulaDescribe+="平均值("
        formulaHistory.push("averageFun(");
        formulaCnHistory.push("平均值(");
      }else if(type === 6){
        computationModel.formulaCode+="absFun("
        computationModel.formulaDescribe+="绝对值("
        formulaHistory.push("absFun(");
        formulaCnHistory.push("绝对值(");
      }else if(type === 7){
        computationModel.formulaCode+="sumFun("
        computationModel.formulaDescribe+="累计值("
        formulaHistory.push("sumFun(");
        formulaCnHistory.push("累计值(");
      }else if(type === 14){
        computationModel.formulaCode+="firstFun("
        computationModel.formulaDescribe+="第一个值("
        formulaHistory.push("firstFun(");
        formulaCnHistory.push("第一个值(");
        //选当前时间不可编辑
        // computationModel.notEdit=true;
      }else if(type === 15){
        computationModel.formulaCode+="lastFun("
        computationModel.formulaDescribe+="最后一个值("
        formulaHistory.push("lastFun(");
        formulaCnHistory.push("最后一个值(");
        //选当前时间不可编辑
        // computationModel.notEdit=true;
      }else if(type === 16){
        computationModel.formulaCode+="timesFun()"
        computationModel.formulaDescribe+="取数信号进入次数()"
        formulaHistory.push("timesFun()");
        formulaCnHistory.push("取数信号进入次数()");
        //选当前时间不可编辑
        // computationModel.notEdit=true;
      }else if(type === 17){
        computationModel.formulaCode="firstTimeFun()"
        computationModel.formulaDescribe="第一个值时间()"
        formulaHistory.push("firstTimeFun()");
        formulaCnHistory.push("第一个值时间()");
        //选当前时间不可编辑
        computationModel.notEdit=true;
      }else if(type === 18){
        computationModel.formulaCode="lastTimeFun()"
        computationModel.formulaDescribe="最后一个值时间()"
        formulaHistory.push("lastTimeFun()");
        formulaCnHistory.push("最后一个值时间()");
        //选当前时间不可编辑
        computationModel.notEdit=true;
      }else if(type === 19){
        computationModel.formulaCode+="ifNullFun("
        computationModel.formulaDescribe+="空值取0("
        formulaHistory.push("ifNullFun(");
        formulaCnHistory.push("空值取0(");
      }else if(type === 30){
        computationModel.formulaCode+="standardFun("
        computationModel.formulaDescribe+="红黄区标准值("
        formulaHistory.push("standardFun(");
        formulaCnHistory.push("红黄区标准值(");
      }else if(type === 31){
        computationModel.formulaCode+="nearFun("
        computationModel.formulaDescribe+="跨行计算("
        formulaHistory.push("nearFun(");
        formulaCnHistory.push("跨行计算(");
        //显示编辑
        computationShow.sortField = true;
      }else if(type === 32){
        computationModel.formulaCode+="firstNotNullFun("
        computationModel.formulaDescribe+="取第一个不为空的PI值("
        formulaHistory.push("firstNotNullFun(");
        formulaCnHistory.push("取第一个不为空的PI值(");
      }else if(type === 33){
        computationModel.formulaCode+="timeSecondFun()"
        computationModel.formulaDescribe+="取数信号合计时间()"
        formulaHistory.push("timeSecondFun()");
        formulaCnHistory.push("取数信号合计时间()");
      }else if(type === 8){
        computationModel.formulaCode+="+"
        computationModel.formulaDescribe+="+"
        formulaHistory.push("+");
        formulaCnHistory.push("+");
      }else if(type === 9){
        computationModel.formulaCode+="-"
        computationModel.formulaDescribe+="-"
        formulaHistory.push("-");
        formulaCnHistory.push("-");
      }else if(type === 10){
        computationModel.formulaCode+="*"
        computationModel.formulaDescribe+="*"
        formulaHistory.push("*");
        formulaCnHistory.push("*");
      }else if(type === 11){
        computationModel.formulaCode+="/"
        computationModel.formulaDescribe+="/"
        formulaHistory.push("/");
        formulaCnHistory.push("/");
      }else if(type === 12){
        computationModel.formulaCode+="("
        computationModel.formulaDescribe+="("
        formulaHistory.push("(");
        formulaCnHistory.push("(");
      }else if(type === 13){
        computationModel.formulaCode+=")"
        computationModel.formulaDescribe+=")"
        formulaHistory.push(")");
        formulaCnHistory.push(")");
      }else if(type === 20){
        computationModel.formulaCode+="0"
        computationModel.formulaDescribe+="0"
        formulaHistory.push("0");
        formulaCnHistory.push("0");
      }else if(type === 21){
        computationModel.formulaCode+="1"
        computationModel.formulaDescribe+="1"
        formulaHistory.push("1");
        formulaCnHistory.push("1");
      }else if(type === 22){
        computationModel.formulaCode+="2"
        computationModel.formulaDescribe+="2"
        formulaHistory.push("2");
        formulaCnHistory.push("2");
      }else if(type === 23){
        computationModel.formulaCode+="3"
        computationModel.formulaDescribe+="3"
        formulaHistory.push("3");
        formulaCnHistory.push("3");
      }else if(type === 24){
        computationModel.formulaCode+="4"
        computationModel.formulaDescribe+="4"
        formulaHistory.push("4");
        formulaCnHistory.push("4");
      }else if(type === 25){
        computationModel.formulaCode+="5"
        computationModel.formulaDescribe+="5"
        formulaHistory.push("5");
        formulaCnHistory.push("5");
      }else if(type === 26){
        computationModel.formulaCode+="6"
        computationModel.formulaDescribe+="6"
        formulaHistory.push("6");
        formulaCnHistory.push("6");
      }else if(type === 27){
        computationModel.formulaCode+="7"
        computationModel.formulaDescribe+="7"
        formulaHistory.push("7");
        formulaCnHistory.push("7");
      }else if(type === 28){
        computationModel.formulaCode+="8"
        computationModel.formulaDescribe+="8"
        formulaHistory.push("8");
        formulaCnHistory.push("8");
      }else if(type === 29){
        computationModel.formulaCode+="9"
        computationModel.formulaDescribe+="9"
        formulaHistory.push("9");
        formulaCnHistory.push("9");
      }else if(type === 99){
        //插入
        if(computationModel.pointCode){
          let formulaCode = computationModel.formulaCode;
          computationModel.formulaCode+="⊙"+computationModel.pointCode+"⊙";
          // let value = getValueByKey(pointArr,computationModel.pointCode)
          let value = getValueByKey(countFieldList,computationModel.pointCode)
          //判断是否包含[取第一个不为空的PI值] 描述需要拼接逗号
          if(computationModel.formulaCode && computationModel.formulaCode.includes("firstNotNullFun(")){
            if(formulaCode === "firstNotNullFun("){
              computationModel.formulaDescribe+= value;
            }else{
              computationModel.formulaDescribe+= ','+value;
            }
          }else{
            computationModel.formulaDescribe+= value;
          }
          formulaHistory.push("⊙"+computationModel.pointCode+"⊙");
          formulaCnHistory.push(value);
        }
      }else if(type === 100){
        //字符插入
        let character = computationModel.character;
        if(character){
          computationModel.formulaCode+=character
          computationModel.formulaDescribe+=character
          formulaHistory.push(character);
          formulaCnHistory.push(character);
          //置空
          computationModel.character = '';
        }
      }

    }
    //清空计算公式
    function cleanFormula(type){
      if(type == 'all'){
        //清空所有
        computationModel.formulaCode = "";
        computationModel.formulaDescribe =""
        computationModel.notEdit=false;
        formulaHistory.length = 0;
        formulaCnHistory.length = 0;
        computationShow.sortField = false;
      }else{
        //退位
        if(computationModel.formulaCode == 'timeFun()' || computationModel.formulaCode == 'dateFun()'){
          computationModel.notEdit=false;
        }
        //匹配最后是否是pi点位
        let target = formulaHistory.pop();
        computationModel.formulaCode = removeOneFromEnd(computationModel.formulaCode,target);

        let targetCn = formulaCnHistory.pop();
        computationModel.formulaDescribe = removeOneFromEnd(computationModel.formulaDescribe,targetCn);
        //判断是否包含[跨行计算]
        if(computationModel.formulaCode && computationModel.formulaCode.includes("nearFun(")){
          computationShow.sortField = true;
        }else{
          computationShow.sortField = false;
        }
        //判断是否包含[取第一个不为空的PI值]
        if(computationModel.formulaCode && computationModel.formulaCode.includes("firstNotNullFun(")){
          computationModel.formulaDescribe = computationModel.formulaDescribe.replace(/,$/, "");
        }
      }
    }

    function removeOneFromEnd(s,target){
      let lastIndex = s.lastIndexOf(target); // 找到 '11' 最后出现的位置
      if (lastIndex !== -1) {
          s = s.slice(0, lastIndex) + s.slice(lastIndex + target.length);
          return s;
      }
      return s
    }

    //补全 " ) "
    function complete(){
      //计算"("有多少个
      let leftParenthesisCount = 0;
      //计算")"有多少个
      let rightParenthesisCount = 0;
      let formulaCode = computationModel.formulaCode
      for (let i = 0; i < formulaCode.length; i++) {
          if (formulaCode[i] === "(") {
              leftParenthesisCount++;
          } else if (formulaCode[i] === ")") {
              rightParenthesisCount++;
          }
      }
      //计算差进行补全
      let difference = leftParenthesisCount - rightParenthesisCount;
      if(difference > 0){
        for (let i = 0; i < difference; i++) {
          formula(13);
        }
      }
    }

    //计算模型-取值来源
    function valueSourceChange(row){
      computationModel.pointCode = '';
      countFieldList.length = 0
      //清空公式与说明
      computationModel.formulaCode = "";
      computationModel.formulaDescribe = "";

      if(row.target.value == 0){
        //基础点位 pointArr
        //countFieldList.value = pointArr.value;
        pointArr.forEach((element, index) => {
            countFieldList[index] = element;
        });
      }else{
        //PI日报
        // countFieldList.value= dailyFieldList.value;
        dailyFieldList.forEach((element, index) => {
            countFieldList[index] = element;
        });
      }
    }

    //计算信号-添加
    function addCalculateSignal(){
      if(calculateSignal.pointCode && calculateSignal.calculateType && calculateSignal.calculateValue){
        let calculateValue = calculateSignal.calculateValue
        if(isStep.value){
          //当前点有对应步骤
          //calculateSignal.explanation = getValueByKey(pointStepArr,calculateSignal.pointCode)+
          calculateSignal.explanation = getValueByKey(pointArr,calculateSignal.pointCode)+
          getValueByKey(countConditionArr,calculateSignal.calculateType)+getValueByKey(stepArr,calculateValue);
        }else{
          //没有对应步骤,手动输入
          // calculateSignal.explanation = getValueByKey(pointStepArr,calculateSignal.pointCode)+
          calculateSignal.explanation = getValueByKey(pointArr,calculateSignal.pointCode)+
          getValueByKey(countConditionArr,calculateSignal.calculateType)+calculateValue;
        }
      }else{
        createMessage.warning("请配置当前点!");
      }                            
    }
    //计算信号-当前点变更
    function pointStepChange(value, option, arr){
      if(value){
        //获取点位对应(全量)-步骤/信号点/生产信息
        getPointStep(value)
      }
    }

  //获取点位对应的步骤
  async function getPointStep(pointCode){
    let res = await pointStep({pointCode: pointCode});
    let newArr = res.map(item => ({
        value: item.stepNum,
        label: item.stepName
    }));
    if(newArr && newArr.length>0){
      isStep.value = true;
    }else{
      calculateSignal.calculateValue = ''
      isStep.value = false;
    }
    stepArr.length = 0;
    stepArr.push(...newArr);
  }
  //唯一约束判断是否可以编辑
  function isUniqueList(val1, val2){
    if(val1 || val2){
      return true;
    }else{
      return false;
    }
  }
  //取数步骤-开始步骤(类型为步骤/时间类型PI)
  const startStepList = reactive<Record<string, any>>([]);
  //取数步骤-结束步骤(类型为步骤/时间类型PI)
  const endStepList = reactive<Record<string, any>>([]);
  //取数步骤-开始步骤
  function startStepChange(val){
    retrievalStepStart.pointCode = ''
    setStartStep(val);
  }
  // 取数步骤-开始步骤设置PI下拉
  function setStartStep(val){
    startStepList.length = 0
    if(val == '3'){
      //PI
      fieldDateTimeList.forEach((element, index) => {
        startStepList.push(element)
      });
    }else{
      //其他
      pointStepArr.forEach((element, index) => {
        startStepList.push(element)
      });  
    }
  }

  //取数步骤-结束步骤
  function endStepChange(val){
    retrievalStepEnd.pointCode = ''
    setEndStep(val)
  }
  // 取数步骤-结束步骤设置PI下拉
  function setEndStep(val){
    endStepList.length = 0
    if(val == '3'){
      //PI
      fieldDateTimeList.forEach((element, index) => {
        endStepList.push(element)
      });
    }else{
      //其他
      pointStepArr.forEach((element, index) => {
        endStepList.push(element)
      });  
    }
  }
  //取数信号-取值类型
  function valueTypeChange(val){
    val.calculateValue = undefined;
    val.calculateType = undefined;
    fetchConditionChange(val);
  }

  //取数信号-条件 fetchDynamicConditionArr  fetchConditionArr  fetchDictConditionArr dictOptions
  function fetchConditionChange(val){
    if(val.fetchDynamicConditionArr){
      val.fetchDynamicConditionArr.length = 0  
    }else{
      val.fetchDynamicConditionArr = []
    }

    if(val.valueType == 0 || val.valueType == 1){
      //值  点位
      fetchConditionArr.forEach((element, index) => {
          val.fetchDynamicConditionArr[index] = element;
      });
    }else{
      // 字典
      fetchDictConditionArr.forEach((element, index) => {
          val.fetchDynamicConditionArr[index] = element;
      });
      val.calculateType = '7';
    }
  }

  //添加唯一约束
  function addUnique(){
    // <a-select v-model:value="val.colCode" show-search option-filter-prop="label" :size="size" class="width-200 margin-right-10" :options="piFieldAllList" placeholder="请选择" allowClear></a-select>
    //             <a-select v-model:value="val.tagCode" show-search option-filter-prop="label" :size="size" :disabled="isUniqueList(val.uniqueValue,val.yearHour)" class="width-200" :options="pointArr" placeholder="请选择" allowClear></a-select>
    //             <span class="margin-left-10 margin-right-10">或</span>
    //             <a-input v-model:value="val.uniqueValue" placeholder="请输入" :size="size" class="width-80" :disabled="isUniqueList(val.tagCode,val.yearHour)" allowClear/>
    //             <span class="margin-left-10 margin-right-10">或</span>
    //             <a-select v-model:value="val.yearHour" :size="size" class="width-120" :disabled="isUniqueList(val.tagCode,val.uniqueValue)" :options="yearHourArr" placeholder="请选择" allowClear @change="function(){val.frequency=''}"></a-select>
    //             <!-- <div> -->
    //             <a-select v-if="val.yearHour == 'minFreFun'" v-model:value="val.frequency" :size="size" class="width-80" :disabled="isUniqueList(val.tagCode,val.uniqueValue)" :options="minFreArr" placeholder="请选择" allowClear></a-select>
    //             <a-time-picker v-if="val.yearHour == 'timeFreFun'" v-model:value="val.frequency" :size="size" class="width-80" :disabled="isUniqueList(val.tagCode,val.uniqueValue)" placeholder="请选择" format="HH:mm" valueFormat="HH:mm" allowClear></a-time-picker>
    //             <span class="del-signal" @click="delUnique(index)" style="cursor: pointer;color:red" title="删除"><CloseCircleOutlined/></span>

    let obj = {"colName":"","colCode":undefined,"tagCode":undefined,"tagValue":""};
    uniqueList.push(obj);
  }

  //删除唯一约束
  function delUnique(index){
    uniqueList.splice(index, 1);
  }
  // 唯一约束修改
  function uniqueChange(val){
    const item = piFieldAllList.find(item => item.value === val.colCode);
    val.colName = item ? item.label : null;
  }

  //计算信号-点位类型变更
  function pointTypeChange(){
    if(calculateSignal.pointType === 0){
      //自动化点位
      calculateSignal.timeType = undefined;
      calculateSignal.timeComment = '';
    }else{
      //时间
      calculateSignal.pointCode = undefined;
      calculateSignal.calculateType = undefined;
      calculateSignal.calculateValue = undefined;
      calculateSignal.calculateValue = '';
    }
  }
  //计算信号-时间类型变更
  function timeTypeChange(){
    if(calculateSignal.timeType !== 2){
      calculateSignal.timeComment = '';
    }
  }
  //计算模型-结果模型转换
  async function getResultModelData(){ 
    let res = await getResultModel({});
    let newArr = res.map(item => ({
        label:item.modelName,
        value:item.id
    }));
    resultModelList.push(...newArr);
  }
  //计算模型-结果模型变更
  function resultModelChange(val,form){
    //通过 val 获取 label
    let label = resultModelList.filter(item => item.value === val)[0].label;
    form.resultModelName = label;
  }
  defineExpose({
      add,
      edit,
      submitForm,
  });


  //筛选
  const filterOption = (input: string, option: any) => {
    let bj = false;
    //下拉匹配
    if(option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0 || 
       option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0){
      bj = true;
    }
    return bj;
  }
</script>

<style lang="less" scoped>
.gutter-box{
  width: 98%;
  height: 200px;
  background-color: #ffffff;
  margin: 0 auto;
  border-radius: 8px;
  color:#000000d9;
}
.gutter-box-top{
  width: 98%;
  height: 300px;
  background-color: #ffffff;
  margin: 0 auto;
  border-radius: 8px;
  color:#000000d9;
}
.gutter-box-bottom{
  width: 99%;
  height: 470px;
  background-color: #ffffff;
  margin: 0 auto;
  border-radius: 8px;
  color:#000000d9;
}
.box-piece-title{
  font-size: 14px;
  font-weight: 600;
  padding: 5px;
  color:#000000d9;
}
.box-piece-content{
  padding: 8px;
}

.margin-right-5{
  margin-right: 5px;
}
.margin-right-10{
  margin-right: 10px;
}
.margin-left-10{
  margin-left: 10px;
}
.margin-left-5{
  margin-left: 5px;
}
.height-30{
  height: 35px;
}
.height-35{
  height: 35px;
}
.height-55{
  height: 55px;
}
.width-25{
  width: 25px;
}
.width-50{
  width: 50px;
}
.width-70{
  width: 70px;
}
.width-80{
  width: 80px;
}
.width-90{
  width: 90px;
}
.width-100{
  width: 100px;
}
.width-120{
  width: 120px;
}
.width-150{
  width: 150px;
}
.width-170{
  width: 170px;
}
.width-200{
  width: 200px;
}
.text-content{
  background-color: #f7f8fa;
  width: 100%;
  margin-top: 5px;
  overflow-y: auto;
}
.text-count-content{
  background-color: #f7f8fa;
  width: 100%;
  margin-top: 5px;
}

.icon-style{
  font-size: 18px;
  color: #1677ff;
}

.title-icon{
  font-size: 14px;
  color: #1677ff;
}

.ant-divider-horizontal {
  display: flex;
  clear: both;
  width: 100%;
  min-width: 100%;
  margin: 0px 0;
}
.divider-style{
  height: 1px; 
  background-color: #f7f8fa;
}
.icon_add_visibility{
  visibility: hidden;
}
//.del-signal:hover{
//  display: block;
//}
// @import '../../common.less';
</style>