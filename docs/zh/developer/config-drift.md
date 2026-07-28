# 配置字段漂移

WebToApp 中最常见的静默失败:某功能预览正常,却在导出的 APK 中被悄悄跳过,原因是配置字段名在导出工厂和 shell 配置类之间发生了漂移。

## 为什么会发生

shell 用 **Gson** 读取配置,而 Gson 会**静默丢弃**未知或缺失的字段 —— 没有异常,没有日志。因此,如果导出工厂写入的 JSON 键与 shell 配置字段上的 `@SerializedName` 不完全匹配,该功能就是……不运行,且毫无报错。

必须保持对齐的三样东西:

1. `ApkConfigJsonFactory.kt` 中的 payload 键(`"key" to value` 对)。
2. `ShellModeManager.kt` 的 shell 配置类中的 `@SerializedName("key")` 注解。
3. 它们所映射的 `ApkConfig` 字段。

## 门禁

有一个 CI 门禁自动检查这一点:

```bash
./gradlew :app:checkConfigFieldDrift --no-configuration-cache
# 或直接:
python3 scripts/check_config_field_drift.py
```

它从 `ApkConfigJsonFactory.kt` 解析出 payload 键,从 `ShellModeManager.kt` 解析出 `@SerializedName` 注解,然后报告任何不匹配(受 `scripts/config_field_drift_allowlist.json` 中的允许清单约束)。

**只要你改了配置字段就运行它。** 它是 Android CI `check` 任务的一部分。

## 诊断清单

当某功能"预览正常、导出失效"时,按此顺序检查:

1. shell 配置 JSON 在运行时真的包含该字段吗?
2. `ApkConfig` JSON 中的字段名与 shell 配置中的 `@SerializedName` 匹配吗?运行 `checkConfigFieldDrift`。
3. 运行时使用点是 shell 同步的(而非宿主专属)吗?
4. 特别针对去广告:确认 `adBlockEnabled` 映射、从缓存订阅重建宿主过滤器,以及导出规则编译时没有清掉宿主状态。
5. 同步改动后重建模板(陈旧模板是常见疏漏)。

## 添加一个影响生成 APK 的设置

追踪并更新**全部**:

1. 模型(`WebApp` / 嵌套配置)与编辑器 UI 绑定。
2. 导出映射(`ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`)。
3. shell 配置类型(`ShellModeManager` / shell 配置数据类),如果运行时读取它们。
4. shell 同步代码中的运行时使用点。
5. 当开关变化时,为导出接线编写单元测试。

漏掉任何一步,通常会导致:编辑器显示开关、导出忽略它,或导出嵌入了运行时从不读取的配置。
