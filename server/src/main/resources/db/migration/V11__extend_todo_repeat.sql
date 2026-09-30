-- 扩展规则及发生项快照；已有规则字段保持NULL，按旧规则解释。
ALTER TABLE ha_todo_rule
  ADD COLUMN repeat_mode VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '重复模式：TIME按时间、AFTER_COMPLETION完成后、FIXED_DATES固定日，旧规则NULL',
  ADD COLUMN repeat_unit VARCHAR(8) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '重复单位：DAY天、WEEK周、MONTH月、YEAR年',
  ADD COLUMN repeat_interval SMALLINT NULL COMMENT '重复间隔，1至365',
  ADD COLUMN week_days VARCHAR(20) NULL COMMENT '周内日期，逗号分隔，1周一至7周日',
  ADD COLUMN month_days VARCHAR(100) NULL COMMENT '月内日期，逗号分隔，1至31',
  ADD COLUMN last_day TINYINT NULL COMMENT '月末选项：0关闭1开启',
  ADD COLUMN year_days VARCHAR(2200) NULL COMMENT '年度公历月日，逗号分隔MM-dd，最多366项',
  ADD COLUMN fixed_dates VARCHAR(1100) NULL COMMENT '固定公历日期，逗号分隔yyyy-MM-dd，最多100项';
ALTER TABLE ha_todo_occurrence
  ADD COLUMN repeat_mode VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '重复模式：TIME按时间、AFTER_COMPLETION完成后、FIXED_DATES固定日，旧规则NULL',
  ADD COLUMN repeat_unit VARCHAR(8) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '重复单位：DAY天、WEEK周、MONTH月、YEAR年',
  ADD COLUMN repeat_interval SMALLINT NULL COMMENT '重复间隔，1至365',
  ADD COLUMN week_days VARCHAR(20) NULL COMMENT '周内日期，逗号分隔，1周一至7周日',
  ADD COLUMN month_days VARCHAR(100) NULL COMMENT '月内日期，逗号分隔，1至31',
  ADD COLUMN last_day TINYINT NULL COMMENT '月末选项：0关闭1开启',
  ADD COLUMN year_days VARCHAR(2200) NULL COMMENT '年度公历月日，逗号分隔MM-dd，最多366项',
  ADD COLUMN fixed_dates VARCHAR(1100) NULL COMMENT '固定公历日期，逗号分隔yyyy-MM-dd，最多100项';
ALTER TABLE ha_todo_rule MODIFY COLUMN recurrence VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '重复规则：ONCE、DAILY、MONTHLY、EVERY_N_MONTHS、YEARLY、CUSTOM自定义';
