export const ADOPT_INSTANCE_STATUS = {
  AVAILABLE: 0,
  ADOPTED: 1,
  FULFILLED: 2,
  DEAD: 3
}

export const ADOPT_INSTANCE_VIEW_TYPE = {
  USER: 'USER',
  FARMER: 'FARMER'
}

export const ADOPT_INSTANCE_STATUS_OPTIONS = [
  { label: '可认养', value: ADOPT_INSTANCE_STATUS.AVAILABLE, color: 'default' },
  { label: '已认养', value: ADOPT_INSTANCE_STATUS.ADOPTED, color: 'gold' },
  { label: '已履约完成', value: ADOPT_INSTANCE_STATUS.FULFILLED, color: 'green' },
  { label: '异常死亡', value: ADOPT_INSTANCE_STATUS.DEAD, color: 'red' }
]

export const ADOPT_LOG_TYPE = {
  FEED: 1,
  WEIGHT_RECORD: 2,
  EPIDEMIC_PREVENTION: 3,
  DAILY_RECORD: 4,
  ABNORMAL_EVENT: 5
}

export const ADOPT_LOG_TYPE_OPTIONS = [
  { label: '喂食', value: ADOPT_LOG_TYPE.FEED, color: 'green' },
  { label: '体重记录', value: ADOPT_LOG_TYPE.WEIGHT_RECORD, color: 'blue' },
  { label: '防疫', value: ADOPT_LOG_TYPE.EPIDEMIC_PREVENTION, color: 'cyan' },
  { label: '日常记录', value: ADOPT_LOG_TYPE.DAILY_RECORD, color: 'default' },
  { label: '异常事件', value: ADOPT_LOG_TYPE.ABNORMAL_EVENT, color: 'red' }
]

export const ADOPT_DIARY_IMAGE_BIZ_CODE = 'ANIMAL_DIARY'
export const ADOPT_DIARY_VIDEO_BIZ_CODE = 'ANIMAL_DIARY_VIDEO'

export const getAdoptInstanceStatusOption = (status) => {
  return ADOPT_INSTANCE_STATUS_OPTIONS.find((item) => item.value === status)
}

export const getAdoptLogTypeOption = (logType) => {
  return ADOPT_LOG_TYPE_OPTIONS.find((item) => item.value === logType)
}
