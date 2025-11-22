package dji.sampleV5.aircraft.pages

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.models.KnowledgeGraphDataGenerator
import dji.sampleV5.aircraft.models.KnowledgeGraphNode
import dji.sampleV5.aircraft.util.ToastUtils

/**
 * 知识图谱展示界面
 * 显示三元节点列表（主体-关系-客体）
 */
class KnowledgeGraphFragment : DJIFragment() {

    private var recyclerView: RecyclerView? = null
    private var searchEdit: EditText? = null
    private var statsText: TextView? = null
    private var emptyView: TextView? = null
    private var loadingView: View? = null
    private var btnBack: ImageButton? = null
    private var btnRefresh: ImageButton? = null

    private lateinit var adapter: KnowledgeGraphAdapter
    private var allNodes: List<KnowledgeGraphNode> = emptyList()
    private var filteredNodes: List<KnowledgeGraphNode> = emptyList()

    private val handler = Handler(Looper.getMainLooper())
    private var loadingRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_knowledge_graph_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initRecyclerView()
        loadData()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.knowledge_graph_recycler)
        searchEdit = view.findViewById(R.id.search_edit)
        statsText = view.findViewById(R.id.stats_text)
        emptyView = view.findViewById(R.id.empty_view)
        loadingView = view.findViewById(R.id.loading_view)
        btnBack = view.findViewById(R.id.btn_back)
        btnRefresh = view.findViewById(R.id.btn_refresh)

        // 返回按钮
        btnBack?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 刷新按钮
        btnRefresh?.setOnClickListener {
            loadData()
            ToastUtils.showToast("数据已刷新")
        }

        // 搜索功能
        searchEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterData(s?.toString() ?: "")
            }
        })
    }

    private fun initRecyclerView() {
        adapter = KnowledgeGraphAdapter(
            nodes = emptyList(),
            onItemClick = { node ->
                showNodeDetails(node)
            }
        )

        recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@KnowledgeGraphFragment.adapter
        }
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        // 显示加载视图
        showLoading()

        // 取消之前的延迟任务
        loadingRunnable?.let { handler.removeCallbacks(it) }

        // 模拟数据处理，5秒后显示结果
        loadingRunnable = Runnable {
            // 从示例数据生成器获取数据
            // 实际应用中可以从数据库、网络或其他数据源加载
            allNodes = KnowledgeGraphDataGenerator.getSampleData()
            filteredNodes = allNodes

            // 隐藏加载视图，显示数据
            hideLoading()
            updateUI()
        }

        handler.postDelayed(loadingRunnable!!, 5000) // 5秒延迟
    }

    /**
     * 显示加载视图
     */
    private fun showLoading() {
        loadingView?.visibility = View.VISIBLE
        recyclerView?.visibility = View.GONE
        searchEdit?.visibility = View.GONE
        statsText?.visibility = View.GONE
        emptyView?.visibility = View.GONE
    }

    /**
     * 隐藏加载视图
     */
    private fun hideLoading() {
        loadingView?.visibility = View.GONE
        recyclerView?.visibility = View.VISIBLE
        searchEdit?.visibility = View.VISIBLE
        statsText?.visibility = View.VISIBLE
    }

    /**
     * 过滤数据
     */
    private fun filterData(query: String) {
        filteredNodes = if (query.isEmpty()) {
            allNodes
        } else {
            allNodes.filter { node ->
                node.subject.contains(query, ignoreCase = true) ||
                node.predicate.contains(query, ignoreCase = true) ||
                node.`object`.contains(query, ignoreCase = true) ||
                node.description.contains(query, ignoreCase = true)
            }
        }

        updateUI()
    }

    /**
     * 更新UI
     */
    private fun updateUI() {
        adapter.updateData(filteredNodes)

        // 更新统计信息
        statsText?.text = "共 ${filteredNodes.size} 个节点"

        // 显示/隐藏空状态
        if (filteredNodes.isEmpty()) {
            emptyView?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        } else {
            emptyView?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
    }

    /**
     * 显示节点详情
     */
    private fun showNodeDetails(node: KnowledgeGraphNode) {
        val message = """
            节点ID: ${node.id}
            
            主体: ${node.subject}
            关系: ${node.predicate}
            客体: ${node.`object`}
            
            描述: ${node.description}
        """.trimIndent()

        ToastUtils.showToast(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 移除所有延迟任务
        loadingRunnable?.let { handler.removeCallbacks(it) }
        loadingRunnable = null

        recyclerView = null
        searchEdit = null
        statsText = null
        emptyView = null
        loadingView = null
        btnBack = null
        btnRefresh = null
    }
}

