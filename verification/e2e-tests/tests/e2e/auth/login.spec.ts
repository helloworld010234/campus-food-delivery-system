import { test, expect } from '@playwright/test'

test.describe('Admin Login Flow', () => {
  test('login page loads', async ({ page }) => {
    await page.goto('/#/login')
    await expect(page.locator('input[type="text"]').first()).toBeVisible()
    await expect(page.locator('input[type="password"]').first()).toBeVisible()
  })

  test('admin can login with valid credentials', async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[type="text"]').first().fill('admin')
    await page.locator('input[type="password"]').first().fill('123456')

    // Wait for API response after clicking login
    const responsePromise = page.waitForResponse(resp =>
      resp.url().includes('/api/employee/login') && resp.status() === 200
    )
    await page.locator('button').filter({ hasText: /登录|Login/i }).click()
    const response = await responsePromise
    const body = await response.json()
    expect(body.code).toBe(1)
    expect(body.data).toHaveProperty('token')

    // Wait for navigation to complete (SPA hash routing)
    await page.waitForTimeout(3000)
    const url = page.url()
    expect(url).toMatch(/dashboard/)
  })

  test('invalid credentials show error', async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[type="text"]').first().fill('admin')
    await page.locator('input[type="password"]').first().fill('wrongpassword')

    const responsePromise = page.waitForResponse(resp =>
      resp.url().includes('/api/employee/login')
    )
    await page.locator('button').filter({ hasText: /登录|Login/i }).click()
    const response = await responsePromise
    const body = await response.json()
    expect(body.code).not.toBe(1)
  })
})
