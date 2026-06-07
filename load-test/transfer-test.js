import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 50 },  // Ramp up to 50 users
        { duration: '1m', target: 50 },   // Stay at 50 users for 1 min
        { duration: '30s', target: 0 },   // Ramp down
    ],
};

const BASE_URL = 'http://localhost:8081';
const SOURCE_WALLET_ID = __ENV.SOURCE_WALLET; // Passed via env var
const TARGET_WALLET_ID = __ENV.TARGET_WALLET; // Passed via env var
const JWT_TOKEN = __ENV.JWT_TOKEN;           // Passed via env var

export default function () {
    const payload = JSON.stringify({
        sourceWalletId: SOURCE_WALLET_ID,
        targetWalletId: TARGET_WALLET_ID,
        amount: 1.00
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${JWT_TOKEN}`,
            'Idempotency-Key': uuidv4() // Generate unique idempotency key per request
        },
    };

    const res = http.post(`${BASE_URL}/api/v1/transfers`, payload, params);

    check(res, {
        'status is 200 or 429': (r) => r.status === 200 || r.status === 429, // Rate limit is acceptable for load test
        'transaction time < 200ms': (r) => r.timings.duration < 200,
    });

    sleep(1);
}
