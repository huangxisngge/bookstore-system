<template>
  <div class="profile-page">
    <h2>个人信息</h2>
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card>
          <template #header><span>基本资料</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="用户名">
              <el-input :value="userStore.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card>
          <template #header><span>会员信息</span></template>
          <div class="member-info">
            <div class="member-level">
              <el-tag :type="userStore.memberLevel === 3 ? 'warning' : userStore.memberLevel === 2 ? '' : 'info'" size="large">
                {{ MEMBER_LEVEL[userStore.memberLevel] || '普通会员' }}
              </el-tag>
            </div>
            <div class="member-detail">
              <p>会员折扣：<strong>{{ (userStore.memberDiscount * 10).toFixed(1) }}折</strong></p>
              <p style="color:#909399;font-size:13px">银卡会员享9.5折，金卡会员享9折</p>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top:16px">
          <template #header><span>修改密码</span></template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPwd" @click="handleChangePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateUser, changePassword } from '@/api/user'
import { ElMessage } from 'element-plus'
import { MEMBER_LEVEL } from '@/utils/constants'

const userStore = useUserStore()
const formRef = ref(null)
const pwdFormRef = ref(null)
const saving = ref(false)
const changingPwd = ref(false)

const form = reactive({ realName: '', phone: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少6个字符', trigger: 'blur' }]
}

onMounted(async () => {
  await userStore.fetchUserInfo()
  const info = userStore.userInfo
  form.realName = info?.realName || ''
  form.phone = info?.phone || ''
  form.email = info?.email || ''
})

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await updateUser(form)
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } finally { saving.value = false }
}

async function handleChangePwd() {
  await pwdFormRef.value.validate()
  changingPwd.value = true
  try {
    await changePassword(pwdForm)
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } finally { changingPwd.value = false }
}
</script>

<style scoped>
h2 { margin: 0 0 20px; color: #303133; }
.member-info { display: flex; align-items: center; gap: 16px; padding: 12px 0; }
.member-level { }
.member-detail p { margin: 4px 0; }
</style>
