import { message } from 'ant-design-vue'

export const handleResp = (
  res,
  successMsg,
  errorMsg,
  onSuccess,
  onError
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
