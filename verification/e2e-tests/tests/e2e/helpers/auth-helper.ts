import type { Page } from '@playwright/test'

export async function loginAsAdmin(page: Page, username = 'admin', password = '123456') {
  await page.goto('/#/login')
  await page.waitForLoadState('networkidle')
  await page.locator('input[type="text"]').first().fill(username)
  await page.locator('input[type="password"]').first().fill(password)

  const responsePromise = page.waitForResponse(
    (resp) => resp.url().includes('/api/employee/login') && resp.status() === 200
  )

  await page.locator('button').filter({ hasText: /登录|Login/i }).click()
  const response = await responsePromise
  const body = await response.json()
  if (body.code !== 1) {
    throw new Error(`Login failed: ${body.msg || JSON.stringify(body)}`)
  }

  // Wait for SPA navigation to dashboard
  await page.waitForTimeout(3000)
}
