<template>
  <div>
    <el-card>
      <div class="toolbar">
        <h3 style="margin:0">分类管理</h3>
        <el-button type="primary" @click="openDialog(null)">新增分类</el-button>
      </div>

      <el-table :data="categories" v-loading="loading" style="width:100%;margin-top:12px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该分类？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategoryList, addCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({ id: null, name: '', description: '' })
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

onMounted(() => loadCategories())

async function loadCategories() {
  loading.value = true
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } finally { loading.value = false }
}

function openDialog(cat) {
  if (cat) {
    isEdit.value = true
    Object.assign(form, cat)
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.description = ''
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) await updateCategory(form)
    else await addCategory({ name: form.name, description: form.description })
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadCategories()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await deleteCategory(id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (e) {
    // 拦截器已弹出后端错误提示（如"该分类下仍有 N 本图书"），此处无需重复提示
  }
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
</style>
