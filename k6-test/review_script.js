// 부하 테스트(Load Test)
// - 미리 결정된 부하 상태에서 시스템의 성능을 검사
// - 다양한 부하 테스트 시나리오에서 페이지에 대해 수신된 응답 시간을 테스트
// - 성능 병목 현상을 해결
// - 사용 중인 인프라가 예상 부하를 견딜 수 있는지 판단하는데 도움

// - 응답 시간, 통과/실패 트랜잭션 수, 메모리 사용률, 평균 대기 시간 및 CPU 사용률
// - 테스트의 목적으로는 동시성, 처리량, 요청시 응답 시간, 회귀 테스트 등이 있습니다.
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

// setup
// - orderItem
// 등록 요청 데이터
export function setup() {

}

export default function () {

    // 리뷰 등록
    const request = JSON.stringify({
        'orderItemId': 1,
        'score': 5,
        'content': 'Good!'
    });

    const url = BASE_URL + '/orders/{orderId}/orderItems/{orderItemId}';
    const res = http.post(url, request, params);
    check(res, {
        'is status CREATED': (r) => {
            if (r.status !== 201) {
                fail();
            }
            return r.status === 201;
        }
    });
}

export function teardown() {

}
// 리뷰 수정
// 리뷰 삭제
