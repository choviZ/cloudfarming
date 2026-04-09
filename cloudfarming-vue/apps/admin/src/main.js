import { createApp } from 'vue'
import store from './store';
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue';
import i18n from './locales'
import 'ant-design-vue/dist/reset.css';
const app = createApp(App)

dayjs.locale('zh-cn')

app.use(store)
app.use(router)
app.use(Antd)
app.use(i18n)
app.mount('#app')
