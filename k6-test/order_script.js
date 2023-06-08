import http from 'k6/http';
import { check, fail, sleep } from 'k6';

/*
export let options = {
    vus: 10,            // 가상의 유저 수
    duration: '1m'      // 테스트 진행 시간
}
export default function () {
    http.get('https://test.k6.io');
    sleep(1);
}
*/

const params = {
    headers: {
        'Content-Type': 'application/json'
    }
};

const BASE_URL = "http://localhost:8081/api/v1/orders";

// setup
export function setup() {

}
export default function () {

    //
    const request = JSON.stringify({
        'orderItemId': 1,
        'score': 5,
        'content': 'Good!'
    });

    const url = BASE_URL + '/orders/{orderId}/orderItems/{orderItemId}';
    const res = http.post(url, request, params);
    check(res, {
        'is status CREATED': () => {
            if (res.status !== 200) {
                console.info('addDelivery result >>> ' + addDelivery.body);
                fail();
            }
            return addDelivery.status === 200;
        }
    });
}

export function teardown() {

}

// 리뷰 수정
// 리뷰 삭제
