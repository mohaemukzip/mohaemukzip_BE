INSERT INTO terms (term_id, title, content, is_required, is_active, created_at, updated_at)
VALUES
(
  1,
  '만 14세 이상입니다 (필수)',
  '본 서비스는 만 14세 이상만 이용할 수 있습니다.',
  true,
  true,
  NOW(),
  NOW()
),
(
  2,
  '서비스 이용약관 동의 (필수)',
  '본 약관은 서비스 이용과 관련된 권리 및 의무를 규정합니다.',
  true,
  true,
  NOW(),
  NOW()
),
(
  3,
  '개인정보 수집 및 이용 동의 (필수)',
  '서비스 제공을 위해 개인정보를 수집 및 이용합니다.',
  true,
  true,
  NOW(),
  NOW()
),
(
  4,
  '광고 및 마케팅 정보 수신 동의 (선택)',
  '이벤트, 할인, 마케팅 정보를 수신할 수 있습니다.',
  false,
  true,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content),
  is_required = VALUES(is_required),
  is_active = VALUES(is_active),
  updated_at = NOW();


-- 가공/유제품
INSERT INTO ingredients
(name, category, unit, weight, created_at, updated_at)
VALUES
    ('가다랑어포', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('누텔라', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('단무지', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('돈까스', 'PROCESSED_DAIRY', 'EACH', 0, NOW(), NOW()),
    ('두유', 'PROCESSED_DAIRY', 'ML', 0, NOW(), NOW()),
    ('딸기잼', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('땅콩버터', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('땅콩잼', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('라이스페이퍼', 'PROCESSED_DAIRY', 'PIECE', 0, NOW(), NOW()),
    ('만두', 'PROCESSED_DAIRY', 'EACH', 0, NOW(), NOW()),
    ('맛살', 'PROCESSED_DAIRY', 'EACH', 0, NOW(), NOW()),
    ('모짜렐라치즈', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('미트볼', 'PROCESSED_DAIRY', 'EACH', 0, NOW(), NOW()),
    ('버터', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('부라타치즈', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('사과잼', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('생크림', 'PROCESSED_DAIRY', 'ML', 0, NOW(), NOW()),
    ('아몬드우유', 'PROCESSED_DAIRY', 'ML', 0, NOW(), NOW()),
    ('연유', 'PROCESSED_DAIRY', 'ML', 0, NOW(), NOW()),
    ('요거트', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('요구르트', 'PROCESSED_DAIRY', 'ML', 0, NOW(), NOW()),
    ('우유', 'PROCESSED_DAIRY', 'ML', 0, NOW(), NOW()),
    ('체다치즈', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('초콜릿', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('치즈', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('크림치즈', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('통조림옥수수', 'PROCESSED_DAIRY', 'CAN', 0, NOW(), NOW()),
    ('통조림참치', 'PROCESSED_DAIRY', 'CAN', 0, NOW(), NOW()),
    ('통조림햄', 'PROCESSED_DAIRY', 'CAN', 0, NOW(), NOW()),
    ('파마산치즈', 'PROCESSED_DAIRY', 'GRAM', 0, NOW(), NOW()),
    ('피자', 'PROCESSED_DAIRY', 'PAN', 0, NOW(), NOW()),
    ('핫도그', 'PROCESSED_DAIRY', 'EACH', 0, NOW(), NOW()),
    ('휘핑크림', 'PROCESSED_DAIRY', 'ML', 0, NOW(), NOW());

-- 육류/계란
INSERT INTO ingredients
(name, category, unit, weight, created_at, updated_at)
VALUES
    ('계란', 'MEAT_EGG', 'EACH', 0, NOW(), NOW()),
    ('국거리소고기', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('다진돼지고기', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('다진소고기', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('닭가슴살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('닭고기', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('닭날개', 'MEAT_EGG', 'EACH', 0, NOW(), NOW()),
    ('닭다리', 'MEAT_EGG', 'EACH', 0, NOW(), NOW()),
    ('대패삼겹살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('돼지 갈매기살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('돼지 뒷다리', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('돼지 등심', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('돼지 목살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('돼지 앞다리살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('돼지 항정상', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('베이컨', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('비엔나소세지', 'MEAT_EGG', 'EACH', 0, NOW(), NOW()),
    ('삼겹살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소 등심', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소 안심', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소 채끝살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소고기', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소고기 갈비', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소고기 목살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소고기 사태', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소고기 앞다리살', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소고기 우둔', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('소세지', 'MEAT_EGG', 'EACH', 0, NOW(), NOW()),
    ('양고기', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('잠봉', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('차돌박이', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('햄', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW()),
    ('훈제오리', 'MEAT_EGG', 'GRAM', 0, NOW(), NOW());

