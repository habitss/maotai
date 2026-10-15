# 茅台B园区数字化平台 · 前端（Vue 3 + TypeScript）

> 项目介绍、架构图与后端说明见 **`main` 分支** [README.md](https://github.com/habitss/maotai/blob/main/README.md)。本分支仅存放前端代码。

## 技术栈

Vue 3 · TypeScript · Vite · Ant Design Vue · Three.js（3D 车间/驾驶舱）· ECharts（看板图表）· 海康 h5player（无插件视频）· Pinia · Jest + ts-jest + jsdom

## 目录速览

```text
src/
├── views/maotai/        # 业务页面：工单、批次追溯、罐区监控、能耗看板、视频监控、车间预览…
│   ├── workOrder/       # 工单任务 + 批次追溯（traceGraph.ts 谱系建图纯模块 + 29 例单测）
│   ├── homeDashboard/   # 驾驶舱看板（energyMock.ts 能耗曲线生成纯模块 + 22 例单测）
│   └── monitor/         # 监控中心（海康 h5player 封装、1/4/9/16 分屏、树形导航、回放）
├── components/          # 平台组件（BasicTable、表单等）
└── assets/              # 静态资源
tests/                   # Jest 前端测试（83 例）
public/h5player/         # 海康 h5player 播放器 codebase
```

## 本地运行

```bash
pnpm install
pnpm dev:calculation   # 开发（calculation 模式，base=/mes3）
pnpm build:calculation # 生产构建
npx jest tests         # 前端测试
```
