<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索书名/作者/ISBN" clearable style="width:240px" @keyup.enter="loadBooks" />
          <el-select v-model="categoryId" placeholder="全部分类" clearable style="width:160px;margin-left:12px" @change="loadBooks">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
        <el-button type="primary" @click="openDialog(null)">新增图书</el-button>
      </div>

      <el-table :data="books" v-loading="loading" style="width:100%;margin-top:12px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="封面" width="70">
          <template #default="{ row }">
            <BookCover :src="row.coverImage" :title="row.title" width="40px" height="56px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="100" show-overflow-tooltip />
        <el-table-column label="价格" width="80">
          <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column label="折扣" width="70">
          <template #default="{ row }">{{ (Number(row.discount) * 10).toFixed(1) }}折</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" />
        <el-table-column prop="sales" label="销量" width="70" />
        <el-table-column label="开抢时间" width="160">
          <template #default="{ row }">
            <template v-if="row.grabStartTime">
              <el-tag v-if="row.grabNotStarted" type="danger" size="small" effect="dark">开抢倒计时</el-tag>
              <el-tag v-else type="success" size="small">已开抢</el-tag>
              <div class="grab-time">{{ formatGrabTime(row.grabStartTime) }}</div>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right" align="center">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, row)">
              <el-button type="primary" text size="small">
                操作<el-icon style="margin-left:2px"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item v-if="row.grabStartTime" command="clearGrab" divided>转为普通销售</el-dropdown-item>
                  <el-dropdown-item command="toggle" divided>
                    {{ row.status === 1 ? '下架' : '上架' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" class="danger-item" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0" style="margin-top:16px;justify-content:center"
        background layout="prev, pager, next, total"
        :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadBooks"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑图书' : '新增图书'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="书名" prop="title"><el-input v-model="form.title" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author"><el-input v-model="form.author" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn"><el-input v-model="form.isbn" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher"><el-input v-model="form.publisher" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="折扣" prop="discount"><el-input-number v-model="form.discount" :min="0.1" :max="1" :step="0.05" :precision="2" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面图片">
              <div class="cover-upload">
                <el-upload
                  class="cover-uploader"
                  :show-file-list="false"
                  :http-request="handleUpload"
                  accept="image/*"
                  :before-upload="beforeUpload"
                >
                  <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" alt="封面预览" />
                  <div v-else class="cover-placeholder">
                    <el-icon :size="24"><Plus /></el-icon>
                    <span>上传封面</span>
                  </div>
                </el-upload>
                <div class="cover-url-row">
                  <el-input v-model="form.coverImage" placeholder="或直接输入图片URL" clearable>
                    <template #append>
                      <el-button type="danger" text :disabled="!form.coverImage" @click="form.coverImage = ''">清除</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="促销价"><el-input-number v-model="form.promoPrice" :min="0" :precision="2" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开抢时间">
              <el-date-picker
                v-model="form.grabStartTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                placeholder="留空=立即在售"
                clearable
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { getBookList, addBook, updateBook, deleteBook, clearGrab } from '@/api/book'
import { getCategoryList } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { formatPrice } from '@/utils/constants'
import BookCover from '@/components/BookCover.vue'
import request from '@/api/request'

const loading = ref(false)
const books = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const categoryId = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)

const defaultForm = {
  id: null, title: '', author: '', isbn: '', publisher: '', categoryId: null,
  price: 0, discount: 1.0, stock: 0, coverImage: '', description: '',
  promoPrice: 0, grabStartTime: null, status: 1
}
const form = reactive({ ...defaultForm })

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

onMounted(async () => {
  const res = await getCategoryList()
  categories.value = res.data || []
  loadBooks()
})

async function loadBooks() {
  loading.value = true
  try {
    const params = { current: currentPage.value, size: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (categoryId.value) params.categoryId = categoryId.value
    const res = await getBookList(params)
    books.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openDialog(book) {
  if (book) {
    isEdit.value = true
    Object.assign(form, book)
  } else {
    isEdit.value = false
    Object.assign(form, defaultForm)
  }
  dialogVisible.value = true
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (file.size > 10 * 1024 * 1024) { ElMessage.error('图片大小不能超过 10MB'); return false }
  return true
}

async function handleUpload({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  try {
    // 复用 request 实例：自动带上登录 token（管理员上传）
    const res = await request.post('/file/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      form.coverImage = res.data
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form, grabStartTime: form.grabStartTime || null }
    if (isEdit.value) await updateBook(payload)
    else await addBook(payload)
    ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
    dialogVisible.value = false
    loadBooks()
  } finally { saving.value = false }
}

function formatGrabTime(ts) {
  if (!ts) return ''
  return String(ts).replace('T', ' ').slice(5, 16)
}

async function toggleStatus(book) {
  await updateBook({ ...book, status: book.status === 1 ? 0 : 1 })
  ElMessage.success('操作成功')
  loadBooks()
}

async function handleClearGrab(book) {
  try {
    await ElMessageBox.confirm(`确定将《${book.title}》转为普通售卖（取消开抢）？`, '转为普通销售', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
  } catch (_) { return }
  await clearGrab(book.id)
  ElMessage.success('已转为普通售卖')
  loadBooks()
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该图书？删除后不可恢复', '删除确认', { type: 'warning' })
  } catch (_) { return }
  await deleteBook(id)
  ElMessage.success('删除成功')
  loadBooks()
}

function handleCommand(cmd, row) {
  if (cmd === 'edit') return openDialog(row)
  if (cmd === 'clearGrab') return handleClearGrab(row)
  if (cmd === 'toggle') return toggleStatus(row)
  if (cmd === 'delete') return handleDelete(row.id)
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.toolbar-left { display: flex; align-items: center; }
.cover-upload { display: flex; gap: 12px; align-items: flex-start; width: 100%; }
.cover-uploader { border: 1px dashed var(--el-border-color); border-radius: 8px; cursor: pointer; flex-shrink: 0; }
.cover-placeholder { width: 100px; height: 140px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; color: #909399; font-size: 13px; }
.cover-preview { width: 100px; height: 140px; object-fit: cover; border-radius: 8px; display: block; }
.cover-url-row { flex: 1; display: flex; align-items: center; }
.grab-time { font-size: 12px; color: #909399; margin-top: 4px; }
</style>

<style>
.danger-item { color: var(--el-color-danger) !important; }
.danger-item:hover { color: #fff !important; background-color: var(--el-color-danger) !important; }
</style>
