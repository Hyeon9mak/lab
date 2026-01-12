import uuid
from datetime import datetime, timedelta
import random

# 음식 목록 (50가지)
foods = [
    ('김치찌개', '얼큰한 김치찌개'),
    ('된장찌개', '구수한 된장찌개'),
    ('불고기', '간장 양념 불고기'),
    ('계란말이', '도톰한 계란말이'),
    ('제육볶음', '매콤한 제육볶음'),
    ('카레', '야채 듬뿍 카레'),
    ('비빔밥', '고추장 비빔밥'),
    ('라면', '얼큰한 라면'),
    ('떡볶이', '매콤달콤 떡볶이'),
    ('파스타', '크림 파스타'),
    ('김치볶음밥', '김치 볶음밥'),
    ('오므라이스', '계란 오므라이스'),
    ('돈까스', '바삭한 돈까스'),
    ('치킨', '에어프라이 치킨'),
    ('샐러드', '닭가슴살 샐러드'),
    ('김밥', '참치 김밥'),
    ('우동', '따뜻한 우동'),
    ('냉면', '시원한 물냉면'),
    ('칼국수', '멸치 칼국수'),
    ('짜장면', '집에서 만든 짜장면'),
    ('짬뽕', '해물 짬뽕'),
    ('탕수육', '새콤달콤 탕수육'),
    ('마파두부', '얼큰한 마파두부'),
    ('볶음밥', '새우 볶음밥'),
    ('스테이크', '미디엄 스테이크'),
    ('햄버거', '수제 햄버거'),
    ('피자', '치즈 피자'),
    ('샌드위치', '클럽 샌드위치'),
    ('타코', '멕시칸 타코'),
    ('부리토', '치킨 부리토'),
    ('리조또', '버섯 리조또'),
    ('그라탕', '치즈 그라탕'),
    ('수프', '양송이 수프'),
    ('찜닭', '매콤 찜닭'),
    ('갈비찜', '간장 갈비찜'),
    ('삼겹살', '구운 삼겹살'),
    ('족발', '쟁반 족발'),
    ('보쌈', '굴 보쌈'),
    ('순대', '순대 볶음'),
    ('곱창', '양념 곱창'),
    ('초밥', '연어 초밥'),
    ('사시미', '모둠 사시미'),
    ('텐동', '새우 텐동'),
    ('라멘', '돈코츠 라멘'),
    ('야키소바', '볶음 야키소바'),
    ('오코노미야키', '일본식 오코노미야키'),
    ('쌀국수', '베트남 쌀국수'),
    ('팟타이', '태국 팟타이'),
    ('카오팟', '볶음밥 카오팟'),
    ('덮밥', '규동 덮밥'),
]

# 날짜 범위: 2015-01-01 ~ 2025-12-31
start_date = datetime(2015, 1, 1)
end_date = datetime(2025, 12, 31)
date_list = []
current_date = start_date

while current_date <= end_date:
    date_list.append(current_date)
    current_date += timedelta(days=1)

# 50,000개 데이터 생성
total_records = 50000
records = []

for i in range(total_records):
    # 랜덤하게 날짜와 음식 선택
    random_date = random.choice(date_list)
    random_food = random.choice(foods)

    # 시간도 랜덤하게 (08:00 ~ 21:00)
    random_hour = random.randint(8, 21)
    random_minute = random.randint(0, 59)
    random_second = random.randint(0, 59)

    cooked_at = random_date.replace(hour=random_hour, minute=random_minute, second=random_second)

    record = (
        str(uuid.uuid4()),
        random_food[0],
        random_food[1],
        cooked_at.strftime('%Y-%m-%d %H:%M:%S'),
        'COOKED'
    )
    records.append(record)

# SQL 파일 생성
output_file = '/Users/hyeon9mak-office/GitHub/Hyeon9mak/lab/spring-batch-partitioning/src/main/resources/db/migration/V2__init_data.sql'

with open(output_file, 'w', encoding='utf-8') as f:
    # 1000개씩 묶어서 INSERT
    batch_size = 1000
    for batch_idx in range(0, total_records, batch_size):
        batch_records = records[batch_idx:batch_idx + batch_size]

        f.write("INSERT INTO cooking_log (id, name, description, cooked_at, status) VALUES\n")

        for idx, record in enumerate(batch_records):
            value_str = f"    ('{record[0]}','{record[1]}','{record[2]}','{record[3]}','{record[4]}')"

            if idx < len(batch_records) - 1:
                f.write(value_str + ",\n")
            else:
                f.write(value_str + ";\n")

        # 배치 사이에 빈 줄 추가
        if batch_idx + batch_size < total_records:
            f.write("\n")

print(f"Successfully generated {total_records} records in {output_file}")
print(f"File size: {len(open(output_file, 'r', encoding='utf-8').read()) / (1024*1024):.2f} MB")
