import { createApp } from 'vue'
import { createPinia } from 'pinia'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue';
import i18n from './locales'
import 'ant-design-vue/dist/reset.css';

dayjs.locale('zh-cn')

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(i18n)
app.mount('#app')
