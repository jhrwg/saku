@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.AddCategoryDialog
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.EntryType

/**
 * 分类管理页：按支出 / 收入 / 转账分开管理，支持一级分类与二级分类。
 * 删除一级分类会连同它的子分类一起删掉。
 */
@Composable
fun CategoryManagePage(
    expenseCategories: List<EntryCategory>,
    incomeCategories: List<EntryCategory>,
    transferCategories: List<EntryCategory>,
    onAddCategory: (EntryType, String, String?) -> Unit,
    onDeleteCategory: (EntryType, String) -> Unit,
    onRenameCategory: (EntryCategory, String) -> Unit = { _, _ -> },
    onSetCategoryIcon: (EntryCategory, String) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val types = EntryType.entries
    val type = types[tabIndex]
    val categories = when (type) {
        EntryType.Expense -> expenseCategories
        EntryType.Income -> incomeCategories
        EntryType.Transfer -> transferCategories
    }
    val roots = categories.filter { it.parentId == null }

    var addRootFor by remember { mutableStateOf<EntryType?>(null) }
    var addChildFor by remember { mutableStateOf<EntryCategory?>(null) }
    var deleteTarget by remember { mutableStateOf<EntryCategory?>(null) }
    var editTarget by remember { mutableStateOf<EntryCategory?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "分类",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    OutlinedButton(
                        onClick = { addRootFor = type },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "新增一级分类")
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                types.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                        label = { Text(text = item.label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SectionCard(title = "现有分类") {
                if (roots.isEmpty()) {
                    Text(
                        text = "还没有分类",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
                roots.forEach { root ->
                    CategoryRow(
                        category = root,
                        childCount = categories.count { it.parentId == root.id },
                        onAddChild = { addChildFor = root },
                        onDelete = { deleteTarget = root },
                        onEdit = { editTarget = root },
                    )
                    categories.filter { it.parentId == root.id }.forEach { child ->
                        ChildRow(
                            category = child,
                            onDelete = { deleteTarget = child },
                            onEdit = { editTarget = child },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "删除一级分类会连同它的子分类一起删除；已经记过的账目仍保留原来的分类名。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    addRootFor?.let { targetType ->
        AddCategoryDialog(
            onDismiss = { addRootFor = null },
            onConfirm = { name ->
                onAddCategory(targetType, name, null)
                addRootFor = null
            },
        )
    }

    addChildFor?.let { parent ->
        AddCategoryDialog(
            onDismiss = { addChildFor = null },
            onConfirm = { name ->
                onAddCategory(type, name, parent.id)
                addChildFor = null
            },
        )
    }

    editTarget?.let { target ->
        com.ve.bookkeeping.ui.components.CategoryEditSheet(
            category = target,
            onDismiss = { editTarget = null },
            onSave = { newName, newIconId ->
                if (newName.isNotBlank() && newName != target.label) {
                    onRenameCategory(target, newName)
                }
                if (newIconId != null) onSetCategoryIcon(target, newIconId)
                editTarget = null
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(text = "删除分类") },
            text = { Text(text = "确定删除「${target.label}」吗？删除后不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteCategory(type, target.id)
                    deleteTarget = null
                }) {
                    Text(text = "删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(text = "取消")
                }
            },
        )
    }
}

@Composable
private fun CategoryRow(
    category: EntryCategory,
    childCount: Int,
    onAddChild: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (childCount > 0) {
                Text(
                    text = "$childCount 个子分类",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "编辑分类",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
        }
        IconButton(onClick = onAddChild) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "新增子分类",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "删除分类",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun ChildRow(
    category: EntryCategory,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.SubdirectoryArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "编辑子分类",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "删除分类",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

