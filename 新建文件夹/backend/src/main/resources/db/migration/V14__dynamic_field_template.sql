-- =============================================
-- V14: 动态表单模板 — 9 种作业类专属字段
-- 来源：《软件设计说明书》第 7 章
-- 说明：农户发布需求时，根据所选作业类型
--       动态加载对应的必填/选填表单字段
-- =============================================

-- -----------------------------------------------
-- 1. 动态表单模板表
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_dynamic_field_template (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    -- 模板适用对象：DEMAND=农户需求 / EQUIPMENT=设备建档 / SERVICE_ITEM=服务项
    template_type   VARCHAR(20)   NOT NULL DEFAULT 'DEMAND' COMMENT '模板类型',
    -- 关联作业大类（对应 SERVICE_CATEGORIES 或 v3_machine_category）
    category_code   VARCHAR(30)   NOT NULL COMMENT '作业大类编码',
    -- 字段定义
    field_key       VARCHAR(50)   NOT NULL COMMENT '字段键名（驼峰命名）',
    field_label     VARCHAR(50)   NOT NULL COMMENT '字段中文标签',
    -- 字段类型：TEXT/NUMBER/SELECT/MULTI_SELECT/BOOLEAN/DATE/TEXTAREA
    field_type      VARCHAR(20)   NOT NULL DEFAULT 'TEXT' COMMENT '字段类型',
    -- 是否必填
    required_flag   TINYINT       NOT NULL DEFAULT 1 COMMENT '1=必填 0=选填',
    -- 选项来源：可为 JSON 数组或字典表名
    -- 例如 '["水田","旱地","山地"]' 或 'v3_crop'
    option_source   VARCHAR(500)  NULL COMMENT '选项来源（JSON数组或字典表名）',
    -- 校验规则（JSON）
    -- 例如 '{"min":0,"max":9999,"unit":"亩"}'
    validation_json VARCHAR(500)  NULL COMMENT '校验规则JSON',
    -- 占位文本
    placeholder     VARCHAR(100)  NULL COMMENT '输入提示文本',
    -- 排序
    display_order   INT           NOT NULL DEFAULT 0 COMMENT '展示顺序',
    -- 区域范围（NULL=全国通用）
    region_scope    VARCHAR(100)  NULL COMMENT '适用区域编码，NULL=全国',
    enabled         TINYINT       NOT NULL DEFAULT 1,
    INDEX idx_v3_dft_type_cat (template_type, category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表单模板';


-- =============================================
-- 2. 初始化数据：9 种作业类的农户需求动态字段
-- =============================================

-- -----------------------------------------------
-- 2.1 耕地整地 (TILLAGE)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'TILLAGE', 'plotType',        '地块类型',       'SELECT',       1, '["水田","旱地","山地","梯田"]',      NULL, '请选择地块类型', 10),
('DEMAND', 'TILLAGE', 'fieldCondition',  '水田/旱地',      'SELECT',       1, '["水田","旱地"]',                    NULL, '当前地况', 20),
('DEMAND', 'TILLAGE', 'terrainType',     '地形',           'SELECT',       1, '["平原","丘陵","山区","盆地"]',      NULL, '请选择地形', 30),
('DEMAND', 'TILLAGE', 'isScattered',     '是否零散地块',    'BOOLEAN',      1, NULL,                                NULL, NULL, 40),
('DEMAND', 'TILLAGE', 'canLargeMachine', '能否进大型机械',  'BOOLEAN',      1, NULL,                                NULL, NULL, 50),
('DEMAND', 'TILLAGE', 'tillDepth',       '期望作业深度',    'SELECT',       0, '["浅耕15cm以内","中耕15-25cm","深松25cm以上"]', NULL, '选填', 60);

-- -----------------------------------------------
-- 2.2 播种插秧 (PLANTING)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'PLANTING', 'cropCode',       '作物品种',       'SELECT',       1, 'v3_crop',                            NULL, '请选择作物', 10),
('DEMAND', 'PLANTING', 'plantMethod',    '种植方式',       'SELECT',       1, '["播种","插秧","移栽"]',              NULL, '请选择种植方式', 20),
('DEMAND', 'PLANTING', 'plotCondition',  '地块条件',       'SELECT',       1, '["已整地","未整地","半整地"]',         NULL, '当前地块状态', 30),
('DEMAND', 'PLANTING', 'needFertSync',   '是否同步施肥',    'BOOLEAN',      0, NULL,                                NULL, NULL, 40),
('DEMAND', 'PLANTING', 'seedProvide',    '种子谁提供',      'SELECT',       0, '["自备","需机手带"]',                 NULL, '选填', 50);

-- -----------------------------------------------
-- 2.3 施肥打药 (SPRAYING)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'SPRAYING', 'scene',          '作业场景',       'SELECT',       1, '["大田","果园","温室/大棚"]',          NULL, '请选择场景', 10),
('DEMAND', 'SPRAYING', 'cropCode',       '作物品种',       'SELECT',       1, 'v3_crop',                            NULL, '请选择作物', 20),
('DEMAND', 'SPRAYING', 'cropHeight',     '作物当前高度',    'SELECT',       0, '["低矮(30cm以下)","中等(30-80cm)","高(80cm以上)"]', NULL, '选填', 30),
('DEMAND', 'SPRAYING', 'preferDrone',    '是否偏向无人机',  'BOOLEAN',      0, NULL,                                NULL, NULL, 40),
('DEMAND', 'SPRAYING', 'pesticideReady', '农药是否自备',    'SELECT',       0, '["自备","需代购"]',                   NULL, '选填', 50);

-- -----------------------------------------------
-- 2.4 灌溉排水 (IRRIGATION)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'IRRIGATION', 'workType',     '作业类型',       'SELECT',       1, '["抽水灌溉","喷灌","滴灌","排水","排涝"]', NULL, '请选择类型', 10),
('DEMAND', 'IRRIGATION', 'hasWaterSrc',  '是否有水源',      'BOOLEAN',      1, NULL,                                NULL, NULL, 20),
('DEMAND', 'IRRIGATION', 'hasPower',     '是否有电源',      'BOOLEAN',      1, NULL,                                NULL, NULL, 30),
('DEMAND', 'IRRIGATION', 'isUrgent',     '是否急排涝',      'BOOLEAN',      0, NULL,                                NULL, NULL, 40);

-- -----------------------------------------------
-- 2.5 收割收获 (HARVEST)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'HARVEST', 'cropCode',        '作物品种',       'SELECT',       1, 'v3_crop',                            NULL, '请选择作物', 10),
('DEMAND', 'HARVEST', 'mudLevel',        '地块泥泞程度',    'SELECT',       1, '["干燥","微湿","较湿","泥泞"]',       NULL, '当前地况', 20),
('DEMAND', 'HARVEST', 'isLodging',       '是否有倒伏',      'BOOLEAN',      1, NULL,                                NULL, NULL, 30),
('DEMAND', 'HARVEST', 'lodgingLevel',    '倒伏程度',       'SELECT',       0, '["轻微","中等","严重"]',               NULL, '有倒伏时选择', 40),
('DEMAND', 'HARVEST', 'isScattered',     '是否零散地块',    'BOOLEAN',      1, NULL,                                NULL, NULL, 50),
('DEMAND', 'HARVEST', 'canLargeMachine', '能否进大型机械',  'BOOLEAN',      1, NULL,                                NULL, NULL, 60);

-- -----------------------------------------------
-- 2.6 秸秆处理 (STRAW)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'STRAW', 'cropCode',          '原作物品种',      'SELECT',       1, 'v3_crop',                            NULL, '收获的什么作物', 10),
('DEMAND', 'STRAW', 'strawMethod',       '处理方式',       'SELECT',       1, '["粉碎还田","打捆离田","脱粒","烘干"]', NULL, '请选择处理方式', 20),
('DEMAND', 'STRAW', 'needSameDay',       '是否当天完成',    'BOOLEAN',      0, NULL,                                NULL, NULL, 30);

-- -----------------------------------------------
-- 2.7 果园作业 (ORCHARD)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'ORCHARD', 'fruitType',       '果树类型',       'SELECT',       1, '["苹果","柑橘","桃/李","梨","葡萄","猕猴桃","其他"]', NULL, '请选择果树', 10),
('DEMAND', 'ORCHARD', 'orchWorkType',    '作业类型',       'SELECT',       1, '["打药","开沟施肥","除草","园内运输"]', NULL, '请选择作业', 20),
('DEMAND', 'ORCHARD', 'treeHeight',      '树高',           'SELECT',       1, '["低矮(2m以下)","中等(2-4m)","高大(4m以上)"]', NULL, '果树高度', 30),
('DEMAND', 'ORCHARD', 'rowSpace',        '行距',           'SELECT',       0, '["窄(2m以下)","中(2-4m)","宽(4m以上)"]', NULL, '树行间距', 40),
('DEMAND', 'ORCHARD', 'slopeLevel',      '坡度',           'SELECT',       0, '["平地","缓坡","陡坡"]',               NULL, '选填', 50);

-- -----------------------------------------------
-- 2.8 运输装卸 (TRANSPORT)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'TRANSPORT', 'cargoType',     '运输对象',       'SELECT',       1, '["粮食","化肥/农药","种子/秧苗","果品","其他"]', NULL, '运什么', 10),
('DEMAND', 'TRANSPORT', 'loadWeight',    '载重需求(吨)',    'NUMBER',       1, NULL, '{"min":0.1,"max":50,"unit":"吨"}', '预估重量', 20),
('DEMAND', 'TRANSPORT', 'distRange',     '运输距离',       'SELECT',       1, '["1km以内","1-5km","5-20km","20km以上"]', NULL, '大概距离', 30),
('DEMAND', 'TRANSPORT', 'needLoading',   '是否需要装卸',    'BOOLEAN',      0, NULL,                                NULL, NULL, 40);

-- -----------------------------------------------
-- 2.9 大棚/设施 (GREENHOUSE)
-- -----------------------------------------------
INSERT INTO v3_dynamic_field_template
(template_type, category_code, field_key, field_label, field_type, required_flag, option_source, validation_json, placeholder, display_order) VALUES
('DEMAND', 'GREENHOUSE', 'ghType',       '温室类型',       'SELECT',       1, '["日光温室","塑料大棚","连栋温室","简易拱棚"]', NULL, '请选择温室类型', 10),
('DEMAND', 'GREENHOUSE', 'ghWorkType',   '作业类型',       'SELECT',       1, '["微耕","灌溉","打药","卷帘安装/维修"]', NULL, '请选择作业', 20),
('DEMAND', 'GREENHOUSE', 'passWidth',    '通行宽度(米)',    'NUMBER',       0, NULL, '{"min":0.5,"max":10,"unit":"米"}', '棚内通道宽度', 30);
