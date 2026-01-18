INSERT INTO terms (title, content, is_required, is_active, created_at, updated_at)
VALUES
(
  '만 14세 이상입니다 (필수)',
  '본 서비스는 만 14세 이상만 이용할 수 있습니다.',
  true,
  true,
  NOW(),
  NOW()
),
(
  '서비스 이용약관 동의 (필수)',
  '본 약관은 서비스 이용과 관련된 권리 및 의무를 규정합니다.',
  true,
  true,
  NOW(),
  NOW()
),
(
  '개인정보 수집 및 이용 동의 (필수)',
  '서비스 제공을 위해 개인정보를 수집 및 이용합니다.',
  true,
  true,
  NOW(),
  NOW()
),
(
  '광고 및 마케팅 정보 수신 동의 (선택)',
  '이벤트, 할인, 마케팅 정보를 수신할 수 있습니다.',
  false,
  true,
  NOW(),
  NOW()
);