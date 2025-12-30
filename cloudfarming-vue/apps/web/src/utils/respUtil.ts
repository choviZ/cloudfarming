import type { Result } from '@/types/common.ts'
import { message } from 'ant-design-vue'

export const handleResp = (
  res: Result<any>,
  successMsg?: string,
  errorMsg?: string,
  onSuccess?: () => void,
  onError?: (res: Result<any>) => void
) => {
  if (res.code === '0') {
    if (successMsg) {
      message.success(successMsg)
    }
    if (onSuccess) {
      onSuccess()
    }
  } else {
    if (errorMsg !== undefined) {
      message.error(errorMsg + (res.message || ''))
    }
    if (onError) {
      onError(res)
    }
  }
}