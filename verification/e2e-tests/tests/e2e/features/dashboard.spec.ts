import { test, expect } from '@playwright/test'

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[type="text"]').first().fill('admin')
    await page.locator('input[type="password"]').first().fill('123456')

    const responsePromise = page.waitForResponse(resp =>
      resp.url().includes('/api/employee/login') && resp.status() === 200
    )
    await page.locator('button').filter({ hasText: /登录|Login/i }).click()
    await responsePromise
    await page.waitForTimeout(3000)
  })

  test('dashboard loads after login', async ({ page }) => {
    const url = page.url()
    expect(url).toMatch(/dashboard/)
    // Verify the page has meaningful content (not just noscript)
    const bodyText = await page.locator('body').textContent()
    expect(bodyText).toBeTruthy()
    expect(bodyText.length).toBeGreaterThan(100)
  })

  test('can navigate to shop table page', async ({ page }) => {
    await page.goto('/#/shopTable')
    await page.waitForLoadState('networkidle')
    const url = page.url()
    expect(url).toMatch(/shopTable/)
    const bodyText = await page.locator('body').textContent()
    expect(bodyText).toBeTruthy()
  })
})
