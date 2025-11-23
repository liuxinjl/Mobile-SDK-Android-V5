package dji.sampleV5.aircraft.pages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.models.KnowledgeGraphNode
import dji.sampleV5.aircraft.utils.ImageLoader
import java.text.SimpleDateFormat
import java.util.*

/**
 * 知识图谱节点列表适配器
 */
class KnowledgeGraphAdapter(
    private var nodes: List<KnowledgeGraphNode> = emptyList(),
    private val onItemClick: (KnowledgeGraphNode) -> Unit = {}
) : RecyclerView.Adapter<KnowledgeGraphAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSceneImage: ImageView = view.findViewById(R.id.iv_scene_image)
        val tvSubject: TextView = view.findViewById(R.id.tv_subject)
        val tvPredicate: TextView = view.findViewById(R.id.tv_predicate)
        val tvObject: TextView = view.findViewById(R.id.tv_object)
        val tvDescription: TextView = view.findViewById(R.id.tv_description)
        val tvTimestamp: TextView = view.findViewById(R.id.tv_timestamp)
        val tvNodeId: TextView = view.findViewById(R.id.tv_node_id)
        val tvCompressionRatio: TextView = view.findViewById(R.id.tv_compression_ratio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_knowledge_graph_node, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val node = nodes[position]

        holder.tvSubject.text = node.subject
        holder.tvPredicate.text = node.predicate
        holder.tvObject.text = node.`object`
        holder.tvDescription.text = node.description
        holder.tvTimestamp.text = dateFormat.format(Date(node.timestamp))
        holder.tvNodeId.text = "ID: ${node.id}"

        // 显示信息压缩比
        holder.tvCompressionRatio.text = "压缩比: ${node.compressionRatio}%"

        // 处理图片显示
        if (!node.imageUrl.isNullOrEmpty()) {
            holder.ivSceneImage.visibility = View.VISIBLE
            // 使用 ImageLoader 加载图片
            ImageLoader.loadImage(holder.ivSceneImage, node.imageUrl)
        } else {
            holder.ivSceneImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(node)
        }
    }

    override fun getItemCount(): Int = nodes.size

    /**
     * 更新数据
     */
    fun updateData(newNodes: List<KnowledgeGraphNode>) {
        nodes = newNodes
        notifyDataSetChanged()
    }

    /**
     * 根据关键词过滤数据
     */
    fun filter(query: String): List<KnowledgeGraphNode> {
        if (query.isEmpty()) {
            return nodes
        }

        return nodes.filter { node ->
            node.subject.contains(query, ignoreCase = true) ||
            node.predicate.contains(query, ignoreCase = true) ||
            node.`object`.contains(query, ignoreCase = true) ||
            node.description.contains(query, ignoreCase = true)
        }
    }
}

