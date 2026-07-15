import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const accepted = new Counter('checkout_accepted');
const baseUrl = __ENV.BASE_URL || 'http://localhost:8084';
const token = __ENV.ACCESS_TOKEN;
const productId = Number(__ENV.PRODUCT_ID || 1);
const targetRate = Number(__ENV.RATE || 100);

http.setResponseCallback(http.expectedStatuses(202, 409));

export const options = {
  scenarios: {
    flash_sale: {
      executor: 'ramping-arrival-rate',
      startRate: 0,
      timeUnit: '1s',
      stages: [
        { target: targetRate, duration: __ENV.RAMP_DURATION || '10s' },
        { target: targetRate, duration: __ENV.SUSTAIN_DURATION || '30s' },
        { target: 0, duration: __ENV.RAMP_DOWN_DURATION || '10s' },
      ],
      preAllocatedVUs: Number(__ENV.PRE_ALLOCATED_VUS || 100),
      maxVUs: Number(__ENV.MAX_VUS || 5000),
    },
  },
  thresholds: {
    http_req_duration: ['p(99)<50'],
    http_req_failed: ['rate<0.01'],
    checkout_accepted: ['count>0'],
  },
};

export function setup() {
  if (!token) throw new Error('ACCESS_TOKEN is required');
}

export default function () {
  const idempotencyKey = `k6-${__VU}-${__ITER}`;
  const response = http.post(`${baseUrl}/api/v1/checkout`, JSON.stringify({ productId, quantity: 1 }), {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey,
    },
    tags: { endpoint: 'checkout' },
  });
  check(response, { 'accepted or sold out': r => r.status === 202 || r.status === 409 });
  if (response.status === 202) accepted.add(1);
}
