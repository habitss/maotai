/**
 * 外接提示灯驱动抽象层 —— 工艺报警闪灯用。
 * <p>页面灯(AlarmCenter header 圆点)走纯 CSS 动画;外接实体灯(工业警示灯柱/继电器)
 * 用户暂未定型号,这里先留接口契约 + 空实现,AlarmCenter 调 setExternalLight 同步驱动。
 * 灯型号定了一处改实现,AlarmCenter 不动。
 *
 * <p><b>未来接真实灯的几条路(等型号定):</b>
 * <ul>
 *   <li><b>网络灯(有 HTTP API)/ 网络继电器</b>:fetch 调灯的开关+颜色接口。
 *       若灯在内网、前端跨域够不着,走后端转发(maotai 加个 /maotai/alarm/light 端点转控)。</li>
 *   <li><b>串口/USB 灯</b>:前端 Web Serial API(navigator.serial,仅 Chrome/Edge + HTTPS +
 *       用户点授权连接)直接写指令;或灯接在工控机/服务器上由后端控(前端够不着客户端 USB)。</li>
 * </ul>
 * 现在空实现 = no-op,验证看 AlarmCenter 调用即可。
 */
import type { AlarmLevel } from './alarm.mock';

/**
 * 驱动外接灯。
 * @param level null=灭灯(无未处理报警/离开页面);critical=红、warning=橙
 */
export function setExternalLight(level: AlarmLevel | null) {
  // TODO: 等外接灯型号确定后接真实驱动(网络灯 HTTP / 串口灯 Web Serial API / 后端转发)
  if (level) {
    // console.debug('[外接灯]', level, '亮');
  } else {
    // console.debug('[外接灯] 灭');
  }
}
