import http from 'k6/http';
import {check, fail} from 'k6';

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

const BASE_URL = "http://localhost:8081/api/v1/reviews";
const ORDER_ID = 1;
const ORDER_ITEM_ID = 1;

// setup
// - orderItem
// 등록 요청 데이터
export function setup() {

}

// 리뷰 등록
function create_review(orderId, orderItemId, score, content) {

    const createRequest = JSON.stringify({
        'orderItemId': orderItemId,
        'score': score,
        'content': content
    });

    const url = BASE_URL + `/orders/${orderId}/orderItems/${orderItemId}`;
    const response = http.post(url, createRequest, params);
    check(response, {
        'is status CREATED': (r) => {
            if (r.status !== 201) {
                fail();
            }
            console.log(r);
            return r.status === 201;
        }
    });
}

function update_review() {

}

function delete_review() {

}

export default function () {



    // 리뷰 수정
    const updateRequest = JSON.stringify({

    });

    // 리뷰 삭제
    http.delete()
}

export function teardown() {

}
