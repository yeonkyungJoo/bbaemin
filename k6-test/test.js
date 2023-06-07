import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
    vus: 10,            // 가상의 유저 수
    duration: '1m'      // 테스트 진행 시간
}
export default function () {
    http.get('https://test.k6.io');
    sleep(1);
}

/*
export default function () {
    const url = '';

    // 리뷰 등록
    const payload = JSON.stringify({
        'orderItemId': 1,
        'score': 5,
        'content': 'Good!'
    });
    const params = {
        headers: {
            'Content-Type': 'application/json'
        }
    };
    http.post(url, payload, params);
}*/
// 리뷰 수정
// 리뷰 삭제
