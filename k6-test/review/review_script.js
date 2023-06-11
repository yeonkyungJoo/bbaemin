import http from 'k6/http';
import sql from 'k6/x/sql';
import {check, fail} from 'k6';

export let options = {
    vus: 1,             // 가상의 유저 수
    duration: '10s',     // 테스트 진행 시간

    thresholds: {
        http_req_duration: ['p(99)<100']
    }
}
/*
export default function () {
    http.get('https://test.k6.io');
    sleep(1);
}
*/

// https://github.com/grafana/xk6-sql
// https://github.com/grafana/xk6-sql/blob/master/examples/mysql_test.js
const db = sql.open('mysql', 'root:1234@tcp(localhost:3306)/bbaemin');
export function setup() {
    db.exec(`
        CREATE TABLE IF NOT EXISTS keyvalues (
            id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
            \`key\` VARCHAR(50) NOT NULL,
            value VARCHAR(50) NULL
            );
    `);

    db.exec("INSERT INTO keyvalues (`key`, value) VALUES('plugin-name', 'k6-plugin-sql');");

    let results = sql.query(db, "SELECT * FROM keyvalues WHERE `key` = ?;", 'plugin-name');
    for (const row of results) {
        console.log(`key: ${String.fromCharCode(...row.key)}, value: ${String.fromCharCode(...row.value)}`);
    }
}

const params = {
    headers: {
        'Content-Type': 'application/json'
    }
};
const BASE_URL = "http://localhost:8081/api/v1/reviews";

// 리뷰 조회
function listReview() {
    const url = `${BASE_URL}`;
    const response = http.get(url, params);
}

// 리뷰 등록
function createReview(orderId, orderItemId, score, content, image) {

    const createRequest = JSON.stringify({
        'orderItemId': orderItemId,
        'score': score,
        'content': content,
        'image': image
    });

    const url = `${BASE_URL}/orders/${orderId}/orderItems/${orderItemId}`;
    const response = http.post(url, createRequest, params);
/*
    check(response, {
        'is status CREATED': (r) => {
            if (r.status !== 201) {
                fail();
            }
            console.log(r);
            console.log(JSON.parse(r.body));
            return r.status === 201;
        }
    });*/
}

// 리뷰 수정
function updateReview(reviewId, score, content, image) {

    const updateRequest = JSON.stringify({
        'score': score,
        'content': content,
        'image': image
    });

    const url = `${BASE_URL}/${reviewId}`;
    const response = http.patch(url, updateRequest, params);
    check(response, {
        'is status OK': (r) => {
            if (r.status !== 200) {
                fail();
            }
            console.log(r);
            console.log(JSON.parse(r.body));
            return r.status === 200;
        }
    });
}

// 리뷰 삭제
function deleteReview(reviewId) {

    const url = `${BASE_URL}/${reviewId}`;
    const response = http.del(url, null, params);
    check(response, {
        'is status OK': (r) => {
            if (r.status !== 200) {
                fail();
            }
            console.log(r);
            console.log(JSON.parse(r.body));
            return r.status === 200;
        }
    });
}

export default function () {
    // createReview(1, 1, 5, "Good!", null);
}

export function teardown() {
    db.close();
}
