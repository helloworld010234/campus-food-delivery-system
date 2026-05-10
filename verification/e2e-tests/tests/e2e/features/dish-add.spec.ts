import { test, expect } from '@playwright/test'
import { loginAsAdmin } from '../helpers/auth-helper'
import path from 'path'

const FIXTURES_DIR = path.join(__dirname, '../fixtures')

test.describe('Dish Add Flow', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page, 'merchant01', '123456')
  })

  test('add a new dish with sprite image via real OSS', async ({ page }) => {
    // 1. Navigate to products page via sidebar
    await page.locator('.sidebar-item').filter({ hasText: /商品管理/ }).click()
    await page.waitForLoadState('networkidle')

    const addBtn = page.locator('button').filter({ hasText: /新增商品/ })
    await expect(addBtn).toBeVisible({ timeout: 10000 })

    // 2. Open add-dish dialog
    await addBtn.click()
    const dialog = page.locator('.dialog-overlay')
    await expect(dialog).toBeVisible()
    await expect(page.locator('.dialog-panel h3')).toHaveText('新增商品')

    // 3. Fill form fields
    await page.locator('input[name="name"]').fill('雪碧')

    const categorySelect = page.locator('select[name="categoryId"]')
    await categorySelect.waitFor({ state: 'visible' })
    const options = await categorySelect.locator('option').allInnerTexts()
    if (options.length <= 1) {
      throw new Error('No dish categories available in database. Please seed at least one category.')
    }
    await categorySelect.selectOption({ index: 1 })

    await page.locator('input[name="price"]').fill('5.00')
    await page.locator('textarea[name="description"]').fill('清爽柠檬味汽水')

    // 4. Upload sprite image — must succeed on real OSS
    const uploadPromise = page.waitForResponse(
      (resp) => resp.url().includes('/api/common/upload') && resp.request().method() === 'POST',
      { timeout: 15000 }
    )

    const fileInput = page.locator('input[type="file"]').first()
    await fileInput.setInputFiles(path.join(FIXTURES_DIR, 'sprite.png'))

    const uploadResponse = await uploadPromise
    expect(uploadResponse.status()).toBe(200)
    const uploadBody = await uploadResponse.json()
    expect(uploadBody.code).toBe(1)
    expect(uploadBody.data).toContain('http') // presigned URL
    console.log('OSS presigned URL:', uploadBody.data)

    // Wait for UI to reflect upload success
    await expect(page.getByText('上传成功').first()).toBeVisible({ timeout: 15000 })

    // Screenshot the upload preview inside the dialog
    await page.screenshot({ path: 'test-results/dish-upload-preview.png' })

    // 5. Submit add-dish form
    const addDishPromise = page.waitForResponse(
      (resp) => resp.url().includes('/api/dish') && resp.request().method() === 'POST',
      { timeout: 15000 }
    )
    await page.locator('#dialogSubmit').click()

    const response = await addDishPromise
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.code).toBe(1)

    // 6. Assert success
    await expect(dialog).not.toBeVisible({ timeout: 10000 })
    await expect(page.locator('.toast-success')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('.toast-success')).toContainText('新增商品成功')

    // 7. Verify dish appears in list
    await expect(page.getByText('雪碧').first()).toBeVisible({ timeout: 10000 })

    // 8. Full-page success screenshot
    await page.screenshot({ path: 'test-results/dish-add-success.png', fullPage: true })
  })
})
