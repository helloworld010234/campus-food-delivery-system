import { test, expect } from '@playwright/test'

test.describe('API Health Checks', () => {
  test('backend user API responds', async ({ request }) => {
    const response = await request.get('/user/shop/status')
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(1)
  })

  test('backend admin API responds', async ({ request }) => {
    const response = await request.post('/api/employee/login', {
      data: { username: 'admin', password: '123456' }
    })
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(1)
    expect(body.data).toHaveProperty('token')
  })

  test('nginx serves frontend index', async ({ request }) => {
    const response = await request.get('/')
    expect(response.status()).toBe(200)
    const body = await response.text()
    expect(body).toContain('<div id=app></div>')
  })
})
