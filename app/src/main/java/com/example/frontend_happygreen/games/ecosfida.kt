package com.example.frontend_happygreen.games

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.frontend_happygreen.api.ApiService
import com.example.frontend_happygreen.api.RetrofitClient
import com.example.frontend_happygreen.api.UpdatePointsRequest
import com.example.frontend_happygreen.data.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colori personalizzati migliorati per il tema dell'app
val EcoGreen = Color(0xFF2E7D32)
val EcoLightGreen = Color(0xFFAED581)
val EcoDarkGreen = Color(0xFF1B5E20)
val EcoBlue = Color(0xFF1565C0)
val EcoLightBlue = Color(0xFF81D4FA)
val EcoDarkBlue = Color(0xFF0D47A1)
val EcoBackground = Color(0xFFF8F9FA)
val EcoError = Color(0xFFD32F2F)
val EcoSuccess = Color(0xFF388E3C)
val EcoWarning = Color(0xFFFF9800)
val EcoCardBackground = Color(0xFFFFFFF)

// Modello dei dati per i rifiuti con informazioni educative più complesse
data class Waste(
    val id: Int,
    val name: String,
    val imageUrl: String, // Cambiato da imageResId
    val decompositionTimeYears: Float,
    val pollutionLevel: Int,
    val prevalenceLevel: Int,
    val carbonFootprint: Float,
    val recyclingComplexity: Int,
    val toxicityLevel: Int,
    val educationalFact: String
)


enum class QuestionType {
    DECOMPOSITION_TIME,
    POLLUTION_LEVEL,
    PREVALENCE_LEVEL,
    CARBON_FOOTPRINT,
    RECYCLING_COMPLEXITY,
    TOXICITY_LEVEL
}

class EcoGameViewModel : ViewModel() {

    private val apiService = RetrofitClient.create(ApiService::class.java)
    private val wasteDatabase = listOf(
        Waste(
            1, "Bottiglia PET 500ml", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhcf3keaga51p8ksq5ctfk%2F1748036414_img_0.webp?st=2025-05-24T03%3A58%3A45Z&se=2025-05-30T04%3A58%3A45Z&sks=b&skt=2025-05-24T03%3A58%3A45Z&ske=2025-05-30T04%3A58%3A45Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=fkwsOd5MkKdgg5nrLW09Fx9dVm3Xj1AuW5zzo66yKkQ%3D&az=oaivgprodscus", 450f, 7, 10, 0.82f, 6, 3,
            "Una bottiglia di plastica PET produce 82g di CO2 durante la produzione"
        ),
        Waste(
            2, "Sacchetto biodegradabile", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhe4d2e87tpgev8aam5xjf%2F1748036470_img_0.webp?st=2025-05-24T03%3A57%3A48Z&se=2025-05-30T04%3A57%3A48Z&sks=b&skt=2025-05-24T03%3A57%3A48Z&ske=2025-05-30T04%3A57%3A48Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=FmjH0xIulpO8axnpjm092iX64NRxYaXnQJPOvS14cpI%3D&az=oaivgprodscus", 0.25f, 3, 7, 0.15f, 8, 2,
            "I sacchetti biodegradabili necessitano di condizioni specifiche per decomporsi"
        ),
        Waste(
            3, "Lattina alluminio 330ml", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhjc3me9qryhwd2k48bmd0%2F1748036609_img_0.webp?st=2025-05-24T03%3A59%3A35Z&se=2025-05-30T04%3A59%3A35Z&sks=b&skt=2025-05-24T03%3A59%3A35Z&ske=2025-05-30T04%3A59%3A35Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=SNfwHL6%2FuJMFXVA2B9SWcQ5sMzjUkki1kuB4sjGV8%2Fs%3D&az=oaivgprodscus", 80f, 5, 9, 1.7f, 9, 2,
            "Riciclare alluminio risparmia il 95% dell'energia rispetto alla produzione primaria"
        ),
        Waste(
            4, "Buccia d'arancia", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhkyj3exd845y5sj7hwx7v%2F1748036642_img_0.webp?st=2025-05-24T03%3A58%3A36Z&se=2025-05-30T04%3A58%3A36Z&sks=b&skt=2025-05-24T03%3A58%3A36Z&ske=2025-05-30T04%3A58%3A36Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=TH4mn7gDKRQN6HvUoYZyxy1tB6YsMQrYeNZ6evmLnN4%3D&az=oaivgprodscus", 0.17f, 1, 6, 0.01f, 10, 1,
            "Gli agrumi contengono oli essenziali che rallentano la decomposizione"
        ),
        Waste(
            5, "Filtro sigaretta", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhnn86f5q8nzg9aac7n57y%2F1748036715_img_0.webp?st=2025-05-24T03%3A57%3A47Z&se=2025-05-30T04%3A57%3A47Z&sks=b&skt=2025-05-24T03%3A57%3A47Z&ske=2025-05-30T04%3A57%3A47Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=G57RRnfXTkmt%2BXCgd4tYY67ZMF8I0A2HLjEYq7E%2B%2BUs%3D&az=oaivgprodscus", 12f, 9, 10, 0.05f, 1, 8,
            "I filtri contengono acetato di cellulosa e oltre 4000 sostanze chimiche"
        ),
        Waste(
            6, "Pannolino usa e getta", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhr9dfe59vybspv3dmma3a%2F1748036803_img_0.webp?st=2025-05-24T03%3A58%3A45Z&se=2025-05-30T04%3A58%3A45Z&sks=b&skt=2025-05-24T03%3A58%3A45Z&ske=2025-05-30T04%3A58%3A45Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=mllzjb14M0hcSOcL6SuUb3kmam7agGUOvOon5UbZvoE%3D&az=oaivgprodscus", 500f, 6, 8, 5.5f, 2, 4,
            "Un bambino usa mediamente 6000 pannolini nei primi 3 anni di vita"
        ),
        Waste(
            7, "Rivista patinata", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzj0atpf2n8yqwjkbn3b272%2F1748037071_img_0.webp?st=2025-05-24T03%3A59%3A03Z&se=2025-05-30T04%3A59%3A03Z&sks=b&skt=2025-05-24T03%3A59%3A03Z&ske=2025-05-30T04%3A59%3A03Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=Uk%2F1U%2Bz9E1Lozh5sMXVkKlQjuafqBsVQ50mp1HskkfM%3D&az=oaivgprodscus", 0.08f, 3, 6, 0.9f, 7, 2,
            "La carta patinata contiene rivestimenti che complicano il riciclaggio"
        ),
        Waste(
            8, "Bicchiere carta plastificato", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzjbw2qf479mxpq41gkw41z%2F1748037453_img_0.webp?st=2025-05-24T03%3A57%3A50Z&se=2025-05-30T04%3A57%3A50Z&sks=b&skt=2025-05-24T03%3A57%3A50Z&ske=2025-05-30T04%3A57%3A50Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=EPrd%2BJV8xSxAiYU1Of2BmPAQ7O6oXK1QxKB8uMpQ3%2Bg%3D&az=oaivgprodscus", 20f, 6, 8, 0.24f, 3, 3,
            "Il rivestimento in polietilene rende questi bicchieri difficili da riciclare"
        ),
        Waste(
            9, "Bottiglia vetro scuro", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzja4ebefy87a5dj9vbt9h6%2F1748037385_img_0.webp?st=2025-05-24T03%3A59%3A27Z&se=2025-05-30T04%3A59%3A27Z&sks=b&skt=2025-05-24T03%3A59%3A27Z&ske=2025-05-30T04%3A59%3A27Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=FbhndjshGOIN3ATMTdnTXLuk5DW73xlc0h0b4%2BscA%2FI%3D&az=oaivgprodscus", 4000f, 3, 6, 0.5f, 9, 2,
            "Il vetro scuro è infinitamente riciclabile senza perdita di qualità"
        ),
        Waste(
            10, "Chewing gum", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzjdrefeqtr79dsr22seq5s%2F1748037511_img_0.webp?st=2025-05-24T03%3A58%3A03Z&se=2025-05-30T04%3A58%3A03Z&sks=b&skt=2025-05-24T03%3A58%3A03Z&ske=2025-05-30T04%3A58%3A03Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=rJvSXoNoMFy59DtHCqvMNpQt2tcwHTRSakQ1sWSI%2FfU%3D&az=oaivgprodscus", 5f, 6, 9, 0.02f, 1, 5,
            "La gomma da masticare è fatta di polimeri sintetici non biodegradabili"
        ),
        Waste(
            11, "Smartphone", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzjmqd0f9msqxcza5weshhg%2F1748037740_img_0.webp?st=2025-05-24T03%3A58%3A45Z&se=2025-05-30T04%3A58%3A45Z&sks=b&skt=2025-05-24T03%3A58%3A45Z&ske=2025-05-30T04%3A58%3A45Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=YPSDXSCwzMcevxJykVFFs4cpOfzJb3v5PIi7rh6xX5I%3D&az=oaivgprodscus", 1000f, 10, 7, 85f, 4, 9,
            "Uno smartphone contiene oltre 60 elementi della tavola periodica"
        ),
        Waste(
            12, "Batteria litio", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzke8kpetqb9tqp99f2dbz5%2F1748038565_img_0.webp?st=2025-05-24T03%3A57%3A53Z&se=2025-05-30T04%3A57%3A53Z&sks=b&skt=2025-05-24T03%3A57%3A53Z&ske=2025-05-30T04%3A57%3A53Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=idqJgPs4O5%2FS%2FF0jRK3N%2FH6dc9kuyV6L99LhWHb9yyw%3D&az=oaivgprodscus", 600f, 10, 6, 12f, 5, 10,
            "Le batterie al litio rilasciano gas tossici se smaltite incorrettamente"
        ),
        Waste(
            13, "Mascherina chirurgica", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzjhetpfy3bkrc1kt1ts435%2F1748037626_img_0.webp?st=2025-05-24T03%3A58%3A41Z&se=2025-05-30T04%3A58%3A41Z&sks=b&skt=2025-05-24T03%3A58%3A41Z&ske=2025-05-30T04%3A58%3A41Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=vokzwhNcjguuRBnOu2caMtKcGqdczDpEEAnTT%2BoRRSY%3D&az=oaivgprodscus", 450f, 7, 9, 0.06f, 2, 4,
            "Le mascherine sono fatte di polipropilene, un tipo di plastica"
        ),
        Waste(
            14, "Capsula caffè alluminio", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzjefsve91t0dw2yeh0ejvs%2F1748037543_img_0.webp?st=2025-05-24T03%3A58%3A36Z&se=2025-05-30T04%3A58%3A36Z&sks=b&skt=2025-05-24T03%3A58%3A36Z&ske=2025-05-30T04%3A58%3A36Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=BdKnHtIHxaHVvezEkwnFgRj%2B8vdShGY7LxT%2FuGV5JCQ%3D&az=oaivgprodscus", 80f, 6, 7, 0.3f, 6, 3,
            "Le capsule miste alluminio-plastica richiedono separazione per il riciclaggio"
        ),
        Waste(
            15, "Scontrino termico", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzjbm47f6pvmr1v5m2hqe59%2F1748037441_img_0.webp?st=2025-05-24T03%3A57%3A50Z&se=2025-05-30T04%3A57%3A50Z&sks=b&skt=2025-05-24T03%3A57%3A50Z&ske=2025-05-30T04%3A57%3A50Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=8KgcAuHgXiQq%2B6hrF18FclySoo0O%2BkdEdtfrVPqbfd8%3D&az=oaivgprodscus", 0.03f, 7, 9, 0.002f, 2, 6,
            "La carta termica contiene BPA, un interferente endocrino"
        ),
        Waste(
            16, "Polistirolo imballaggio", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzj6rm1ey2rj50txrakxjpq%2F1748037287_img_0.webp?st=2025-05-24T03%3A59%3A27Z&se=2025-05-30T04%3A59%3A27Z&sks=b&skt=2025-05-24T03%3A59%3A27Z&ske=2025-05-30T04%3A59%3A27Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=iUEkkHbW3CxyyAajJTJIxMkbvslF4s4wagjon%2FD%2BcDU%3D&az=oaivgprodscus", 500f, 8, 8, 1.8f, 2, 4,
            "Il polistirolo è composto al 95% da aria ma è praticamente non riciclabile"
        ),
        Waste(
            17, "Tessuto sintetico", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzj4qv9f1789rprpxppvyaa%2F1748037187_img_0.webp?st=2025-05-24T03%3A59%3A27Z&se=2025-05-30T04%3A59%3A27Z&sks=b&skt=2025-05-24T03%3A59%3A27Z&ske=2025-05-30T04%3A59%3A27Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=zkD0xcgAaEgxs%2FnAS8AdyrurRLsL20p49c8nsOEQ9hY%3D&az=oaivgprodscus", 200f, 6, 7, 9.5f, 3, 5,
            "I tessuti sintetici rilasciano microplastiche ad ogni lavaggio"
        ),
        Waste(
            18, "Pneumatico auto", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhzw8ffaytdrj0geh9de0s%2F1748037055_img_0.webp?st=2025-05-24T04%3A52%3A50Z&se=2025-05-30T05%3A52%3A50Z&sks=b&skt=2025-05-24T04%3A52%3A50Z&ske=2025-05-30T05%3A52%3A50Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=B0r6CG3Tmp3t3gBbvJ3j6gUHkaOcpr%2F2ebwZxyp6k48%3D&az=oaivgprodscus", 2000f, 8, 4, 45f, 5, 6,
            "Un pneumatico può essere trasformato in asfalto o energia"
        ),
        Waste(
            19, "Lampadina LED", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhkr5dfk5bp9e9ezxj3xwj%2F1748036656_img_0.webp?st=2025-05-24T04%3A53%3A04Z&se=2025-05-30T05%3A53%3A04Z&sks=b&skt=2025-05-24T04%3A53%3A04Z&ske=2025-05-30T05%3A53%3A04Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=GSN82dpcjmZ5o2NjhVS4yvck0Vt2efibtDOBtjHHhMI%3D&az=oaivgprodscus", 25f, 4, 5, 2.1f, 7, 4,
            "Le LED durano 25 volte più delle lampadine tradizionali"
        ),
        Waste(
            20, "Cibo scaduto confezionato", "https://videos.openai.com/vg-assets/assets%2Ftask_01jvzhfhbsfsysf8qfqtp4rwwa%2F1748036534_img_0.webp?st=2025-05-24T04%3A53%3A04Z&se=2025-05-30T05%3A53%3A04Z&sks=b&skt=2025-05-24T04%3A53%3A04Z&ske=2025-05-30T05%3A53%3A04Z&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skoid=aa5ddad1-c91a-4f0a-9aca-e20682cc8969&skv=2019-02-02&sv=2018-11-09&sr=b&sp=r&spr=https%2Chttp&sig=i9po5OmY9QtX8nYemw2aRu37ncpQnkjIG3aNQasMQ%2Fc%3D&az=oaivgprodscus", 0.5f, 5, 8, 2.3f, 6, 3,
            "Il 30% del cibo prodotto nel mondo viene sprecato"
        )
    )

    // Stato del gioco
    private var _leftWaste = mutableStateOf<Waste?>(null)
    val leftWaste: State<Waste?> = _leftWaste

    private var _rightWaste = mutableStateOf<Waste?>(null)
    val rightWaste: State<Waste?> = _rightWaste

    private var _currentQuestion = mutableStateOf<QuestionType>(QuestionType.DECOMPOSITION_TIME)
    val currentQuestion: State<QuestionType> = _currentQuestion

    private var _currentScore = mutableStateOf(0)
    val currentScore: State<Int> = _currentScore

    private var _highScore = mutableStateOf(0)
    val highScore: State<Int> = _highScore

    private var _gameOver = mutableStateOf(false)
    val gameOver: State<Boolean> = _gameOver

    private var _gameOverReason = mutableStateOf("")
    val gameOverReason: State<String> = _gameOverReason

    private var _currentLevel = mutableStateOf(1)
    val currentLevel: State<Int> = _currentLevel

    private var _showResult = mutableStateOf(false)
    val showResult: State<Boolean> = _showResult

    private var _lastChoiceCorrect = mutableStateOf(false)
    val lastChoiceCorrect: State<Boolean> = _lastChoiceCorrect

    private val usedWasteIds = mutableSetOf<Int>()

    private var _showTutorial = mutableStateOf(true)
    val showTutorial: State<Boolean> = _showTutorial

    private var _comboStreak = mutableStateOf(0)
    val comboStreak: State<Int> = _comboStreak

    private var _timeRemaining = mutableStateOf(15)
    val timeRemaining: State<Int> = _timeRemaining

    private var _timerActive = mutableStateOf(false)
    val timerActive: State<Boolean> = _timerActive

    // Nuove variabili per animazioni
    private var _showConfetti = mutableStateOf(false)
    val showConfetti: State<Boolean> = _showConfetti

    private var _perfectStreak = mutableStateOf(0)
    val perfectStreak: State<Int> = _perfectStreak

    init {
        startNewRound()
    }

    fun sendScoreToServer(onSuccess: (Int) -> Unit, onError: (String) -> Unit = {}) {
        val gameId = "eco_sfida"
        val scoreToSend = currentScore.value

        viewModelScope.launch {
            try {
                val token = UserSession.getAuthHeader() ?: run {
                    onError("Non sei autenticato")
                    return@launch
                }

                val request = UpdatePointsRequest(
                    points = scoreToSend,
                    game_id = gameId
                )

                val response = apiService.updateUserPoints(token, request)

                if (response.isSuccessful && response.body() != null) {
                    val totalPoints = response.body()!!.total_points
                    UserSession.setEcoPoints(totalPoints)
                    onSuccess(totalPoints)
                } else {
                    onError("Errore nell'invio del punteggio: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("EcoGameViewModel", "Error sending score: ${e.message}")
                onError("Errore di connessione: ${e.message}")
            }
        }
    }


    fun startNewRound() {
        // Progressione livelli più graduale
        when {
            currentScore.value >= 30 && _currentLevel.value < 4 -> _currentLevel.value = 4
            currentScore.value >= 20 && _currentLevel.value < 3 -> _currentLevel.value = 3
            currentScore.value >= 10 && _currentLevel.value < 2 -> _currentLevel.value = 2
        }

        // Timer basato sul livello
        _timeRemaining.value = when(_currentLevel.value) {
            1 -> 15
            2 -> 12
            3 -> 9
            else -> 6
        }

        // Selezione intelligente dei rifiuti per aumentare la difficoltà
        var availableWastes = wasteDatabase.filter { it.id !in usedWasteIds }

        if (availableWastes.size < 2) {
            usedWasteIds.clear()
            availableWastes = wasteDatabase
        }

        // Seleziona tipo di domanda con maggiore varietà
        _currentQuestion.value = QuestionType.values().random()

        // Per livelli alti, seleziona rifiuti con valori più simili (maggiore difficoltà)
        val left = availableWastes.random()
        usedWasteIds.add(left.id)

        val right = if (_currentLevel.value >= 3) {
            // Trova un rifiuto con valori simili per aumentare la difficoltà
            val similars = availableWastes.filter { waste ->
                waste.id != left.id && when(_currentQuestion.value) {
                    QuestionType.DECOMPOSITION_TIME ->
                        kotlin.math.abs(waste.decompositionTimeYears - left.decompositionTimeYears) < (left.decompositionTimeYears * 0.5f)
                    QuestionType.POLLUTION_LEVEL ->
                        kotlin.math.abs(waste.pollutionLevel - left.pollutionLevel) <= 2
                    QuestionType.PREVALENCE_LEVEL ->
                        kotlin.math.abs(waste.prevalenceLevel - left.prevalenceLevel) <= 2
                    QuestionType.CARBON_FOOTPRINT ->
                        kotlin.math.abs(waste.carbonFootprint - left.carbonFootprint) < (left.carbonFootprint * 0.5f)
                    QuestionType.RECYCLING_COMPLEXITY ->
                        kotlin.math.abs(waste.recyclingComplexity - left.recyclingComplexity) <= 2
                    QuestionType.TOXICITY_LEVEL ->
                        kotlin.math.abs(waste.toxicityLevel - left.toxicityLevel) <= 2
                }
            }
            if (similars.isNotEmpty()) similars.random() else availableWastes.filter { it.id != left.id }.random()
        } else {
            availableWastes.filter { it.id != left.id }.random()
        }

        usedWasteIds.add(right.id)

        _leftWaste.value = left
        _rightWaste.value = right
        _showResult.value = false
        _timerActive.value = true
    }

    fun makeSelection(selectedLeft: Boolean) {
        _timerActive.value = false

        val correct = isCorrectAnswer(selectedLeft)
        _lastChoiceCorrect.value = correct
        _showResult.value = true

        if (correct) {
            _comboStreak.value += 1
            _perfectStreak.value += 1

            // Sistema di punteggio più complesso
            val basePoints = when(_currentLevel.value) {
                1 -> 1
                2 -> 2
                3 -> 3
                else -> 4
            }

            val comboBonus = minOf(_comboStreak.value / 2, 5)
            val timeBonus = (_timeRemaining.value / 3)
            val difficultyBonus = if (_currentLevel.value >= 3) 2 else 0

            val totalPoints = basePoints + comboBonus + timeBonus + difficultyBonus
            _currentScore.value += totalPoints

            // Attiva confetti per streak lunghi
            if (_perfectStreak.value >= 5) {
                _showConfetti.value = true
                viewModelScope.launch {
                    delay(2000)
                    _showConfetti.value = false
                }
            }
        } else {
            _comboStreak.value = 0
            _perfectStreak.value = 0
            setGameOverReason()

            if (_currentScore.value > _highScore.value) {
                _highScore.value = _currentScore.value
            }
            _gameOver.value = true
        }
    }

    private fun isCorrectAnswer(selectedLeft: Boolean): Boolean {
        val left = _leftWaste.value ?: return false
        val right = _rightWaste.value ?: return false

        return when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME ->
                if (left.decompositionTimeYears > right.decompositionTimeYears) selectedLeft else !selectedLeft
            QuestionType.POLLUTION_LEVEL ->
                if (left.pollutionLevel > right.pollutionLevel) selectedLeft else !selectedLeft
            QuestionType.PREVALENCE_LEVEL ->
                if (left.prevalenceLevel > right.prevalenceLevel) selectedLeft else !selectedLeft
            QuestionType.CARBON_FOOTPRINT ->
                if (left.carbonFootprint > right.carbonFootprint) selectedLeft else !selectedLeft
            QuestionType.RECYCLING_COMPLEXITY ->
                if (left.recyclingComplexity < right.recyclingComplexity) selectedLeft else !selectedLeft
            QuestionType.TOXICITY_LEVEL ->
                if (left.toxicityLevel > right.toxicityLevel) selectedLeft else !selectedLeft
        }
    }

    private fun setGameOverReason() {
        val left = _leftWaste.value
        val right = _rightWaste.value

        if (left == null || right == null) {
            _gameOverReason.value = "Errore imprevisto."
            return
        }

        when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> {
                val correct = if (left.decompositionTimeYears > right.decompositionTimeYears) left else right
                val correctTime = formatTime(correct.decompositionTimeYears)
                _gameOverReason.value = "${correct.name} impiega $correctTime a decomporsi"
            }
            QuestionType.CARBON_FOOTPRINT -> {
                val correct = if (left.carbonFootprint > right.carbonFootprint) left else right
                _gameOverReason.value = "${correct.name} produce ${correct.carbonFootprint}kg di CO2"
            }
            QuestionType.RECYCLING_COMPLEXITY -> {
                val correct = if (left.recyclingComplexity < right.recyclingComplexity) left else right
                _gameOverReason.value = "${correct.name} è più facile da riciclare (${correct.recyclingComplexity}/10)"
            }
            QuestionType.TOXICITY_LEVEL -> {
                val correct = if (left.toxicityLevel > right.toxicityLevel) left else right
                _gameOverReason.value = "${correct.name} è più tossico (${correct.toxicityLevel}/10)"
            }
            else -> {
                val correct = if (left.pollutionLevel > right.pollutionLevel) left else right
                _gameOverReason.value = "${correct.name} inquina di più (${correct.pollutionLevel}/10)"
            }
        }
    }

    private fun formatTime(years: Float): String {
        return when {
            years < 1 -> "${(years * 12).toInt()} mesi"
            years < 2 -> "${(years * 12).toInt()} mesi"
            else -> "${years.toInt()} anni"
        }
    }

    fun continueGame() {
        startNewRound()
    }

    fun resetGame() {
        _currentScore.value = 0
        _gameOver.value = false
        _currentLevel.value = 1
        _comboStreak.value = 0
        _perfectStreak.value = 0
        _showConfetti.value = false
        usedWasteIds.clear()
        startNewRound()
    }

    fun dismissTutorial() {
        _showTutorial.value = false
    }

    fun updateTimer() {
        if (_timerActive.value && _timeRemaining.value > 0) {
            _timeRemaining.value -= 1

            if (_timeRemaining.value == 0) {
                _timerActive.value = false
                if (_currentScore.value > _highScore.value) {
                    _highScore.value = _currentScore.value
                }
                _gameOverReason.value = "Tempo scaduto! Rispondi più velocemente."
                _gameOver.value = true
                _showResult.value = true
                _lastChoiceCorrect.value = false
            }
        }
    }

    fun getCurrentQuestionText(): String {
        return when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> "Quale impiega PIÙ TEMPO a decomporsi?"
            QuestionType.POLLUTION_LEVEL -> "Quale INQUINA DI PIÙ l'ambiente?"
            QuestionType.PREVALENCE_LEVEL -> "Quale è PIÙ PRESENTE sulla Terra?"
            QuestionType.CARBON_FOOTPRINT -> "Quale ha MAGGIOR IMPATTO sul clima?"
            QuestionType.RECYCLING_COMPLEXITY -> "Quale è PIÙ FACILE da riciclare?"
            QuestionType.TOXICITY_LEVEL -> "Quale è PIÙ TOSSICO per l'ambiente?"
        }
    }

    fun getCorrectAnswerText(): String {
        val left = _leftWaste.value
        val right = _rightWaste.value

        if (left == null || right == null) return ""

        val correctWaste = when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME ->
                if (left.decompositionTimeYears > right.decompositionTimeYears) left else right
            QuestionType.POLLUTION_LEVEL ->
                if (left.pollutionLevel > right.pollutionLevel) left else right
            QuestionType.PREVALENCE_LEVEL ->
                if (left.prevalenceLevel > right.prevalenceLevel) left else right
            QuestionType.CARBON_FOOTPRINT ->
                if (left.carbonFootprint > right.carbonFootprint) left else right
            QuestionType.RECYCLING_COMPLEXITY ->
                if (left.recyclingComplexity < right.recyclingComplexity) left else right
            QuestionType.TOXICITY_LEVEL ->
                if (left.toxicityLevel > right.toxicityLevel) left else right
        }

        return "Risposta: ${correctWaste.name}"
    }

    fun getDetailedComparison(): String {
        val left = _leftWaste.value
        val right = _rightWaste.value

        if (left == null || right == null) return ""

        return when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME -> {
                "${left.name}: ${formatTime(left.decompositionTimeYears)} vs ${right.name}: ${formatTime(right.decompositionTimeYears)}"
            }
            QuestionType.CARBON_FOOTPRINT -> {
                "${left.name}: ${left.carbonFootprint}kg CO2 vs ${right.name}: ${right.carbonFootprint}kg CO2"
            }
            QuestionType.RECYCLING_COMPLEXITY -> {
                "${left.name}: ${left.recyclingComplexity}/10 vs ${right.name}: ${right.recyclingComplexity}/10\n" +
                        "(Più basso = più facile da riciclare)"
            }
            QuestionType.TOXICITY_LEVEL -> {
                "${left.name}: ${left.toxicityLevel}/10 vs ${right.name}: ${right.toxicityLevel}/10\n" +
                        "(Più alto = più tossico)"
            }
            else -> {
                "${left.name}: ${left.pollutionLevel}/10 vs ${right.name}: ${right.prevalenceLevel}/10"
            }
        }
    }

    fun getEducationalFact(): String {
        val left = _leftWaste.value
        val right = _rightWaste.value

        if (left == null || right == null) return ""

        return when (_currentQuestion.value) {
            QuestionType.DECOMPOSITION_TIME ->
                if (left.decompositionTimeYears > right.decompositionTimeYears) left.educationalFact else right.educationalFact
            QuestionType.POLLUTION_LEVEL ->
                if (left.pollutionLevel > right.pollutionLevel) left.educationalFact else right.educationalFact
            QuestionType.PREVALENCE_LEVEL ->
                if (left.prevalenceLevel > right.prevalenceLevel) left.educationalFact else right.educationalFact
            QuestionType.CARBON_FOOTPRINT ->
                if (left.carbonFootprint > right.carbonFootprint) left.educationalFact else right.educationalFact
            QuestionType.RECYCLING_COMPLEXITY ->
                if (left.recyclingComplexity < right.recyclingComplexity) left.educationalFact else right.educationalFact
            QuestionType.TOXICITY_LEVEL ->
                if (left.toxicityLevel > right.toxicityLevel) left.educationalFact else right.educationalFact
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoGameScreen(
    onBack: () -> Unit = {},
    viewModel: EcoGameViewModel = viewModel()
) {
    val leftWaste by viewModel.leftWaste
    val rightWaste by viewModel.rightWaste
    val currentScore by viewModel.currentScore
    val highScore by viewModel.highScore
    val gameOver by viewModel.gameOver
    val showResult by viewModel.showResult
    val lastChoiceCorrect by viewModel.lastChoiceCorrect
    val currentQuestion = viewModel.getCurrentQuestionText()
    val currentLevel by viewModel.currentLevel
    val comboStreak by viewModel.comboStreak
    val timeRemaining by viewModel.timeRemaining
    val timerActive by viewModel.timerActive
    val showTutorial by viewModel.showTutorial
    val showConfetti by viewModel.showConfetti
    val perfectStreak by viewModel.perfectStreak

    // Timer
    LaunchedEffect(timerActive) {
        while (timerActive) {
            delay(1000L)
            viewModel.updateTimer()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Sfondo gradiente
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(EcoBackground, Color.White)
                    )
                )
        ) {
            if (gameOver) {
                GameOverScreen(
                    score = currentScore,
                    onRestart = { viewModel.resetGame() },
                    onBack = onBack,
                    viewModel = viewModel
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Barra superiore migliorata
                    EnhancedTopBar(currentScore, highScore, currentLevel, comboStreak, timeRemaining, perfectStreak)

                    // Domanda con animazione
                    AnimatedQuestionHeader(currentQuestion)

                    // Area di gioco principale con animazioni
                    Row(modifier = Modifier.weight(1f)) {
                        EnhancedWasteCard(
                            waste = leftWaste,
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            cardColor = EcoLightGreen,
                            borderColor = EcoGreen,
                            onClick = { viewModel.makeSelection(true) },
                            enabled = !showResult && !showTutorial,
                            isLeft = true
                        )

                        EnhancedWasteCard(
                            waste = rightWaste,
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            cardColor = EcoLightBlue,
                            borderColor = EcoBlue,
                            onClick = { viewModel.makeSelection(false) },
                            enabled = !showResult && !showTutorial,
                            isLeft = false
                        )
                    }

                    // Area risultati migliorata
                    if (showResult) {
                        EnhancedResultSection(
                            correct = lastChoiceCorrect,
                            correctAnswerText = viewModel.getCorrectAnswerText(),
                            educationalFact = viewModel.getEducationalFact(),
                            detailedComparison = viewModel.getDetailedComparison(),
                            onContinue = { viewModel.continueGame() }
                        )
                    } else if (!showTutorial) {
                        EnhancedInstructionFooter()
                    }
                }
            }
        }

        // Effetti speciali
        if (showConfetti) {
            ConfettiAnimation()
        }

        // Tutorial migliorato
        if (showTutorial) {
            EnhancedTutorialOverlay(onDismiss = { viewModel.dismissTutorial() })
        }
    }
}

@Composable
fun EnhancedTopBar(
    currentScore: Int,
    highScore: Int,
    currentLevel: Int,
    comboStreak: Int,
    timeRemaining: Int,
    perfectStreak: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGreen),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Punteggio con icona
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$currentScore",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "Record: $highScore",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Combo con animazione
                if (comboStreak >= 3) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Combo $comboStreak",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = "Combo: $comboStreak",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Perfect streak
                if (perfectStreak >= 5) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Perfect: $perfectStreak",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Timer migliorato
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val timerColor = when {
                        timeRemaining <= 3 -> Color.Red
                        timeRemaining <= 6 -> EcoWarning
                        else -> Color.White
                    }

                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = timerColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$timeRemaining",
                        color = timerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra del tempo con gradiente
            LinearProgressIndicator(
                progress = timeRemaining / 15f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    timeRemaining <= 3 -> Color.Red
                    timeRemaining <= 6 -> EcoWarning
                    else -> Color.Green
                },
                trackColor = Color.DarkGray
            )
        }
    }
}

@Composable
fun AnimatedQuestionHeader(question: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = EcoCardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            EcoLightGreen.copy(alpha = 0.3f + shimmer * 0.2f),
                            EcoLightBlue.copy(alpha = 0.3f + shimmer * 0.2f)
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = question,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EcoDarkGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EnhancedWasteCard(
    waste: Waste?,
    modifier: Modifier = Modifier,
    cardColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLeft: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val rotation by animateFloatAsState(
        targetValue = if (enabled) 0f else if (isLeft) -2f else 2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation
            )
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(3.dp, borderColor)
    ) {
        waste?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Immagine con ombra migliorata
                Card(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = it.imageUrl,
                            contentDescription = it.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(8.dp),
                            onError = { Log.e("ImageLoad", "Errore nel caricamento immagine da: ") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nome con stile migliorato
                Text(
                    text = it.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = EcoDarkGreen,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Indicatori visivi per le caratteristiche
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Indicatore tempo decomposizione
                    Surface(
                        shape = CircleShape,
                        color = when {
                            it.decompositionTimeYears > 100 -> Color.Red
                            it.decompositionTimeYears > 10 -> EcoWarning
                            else -> Color.Green
                        },
                        modifier = Modifier.size(8.dp)
                    ) {}

                    // Indicatore inquinamento
                    Surface(
                        shape = CircleShape,
                        color = when {
                            it.pollutionLevel > 7 -> Color.Red
                            it.pollutionLevel > 4 -> EcoWarning
                            else -> Color.Green
                        },
                        modifier = Modifier.size(8.dp)
                    ) {}

                    // Indicatore tossicità
                    Surface(
                        shape = CircleShape,
                        color = when {
                            it.toxicityLevel > 7 -> Color.Red
                            it.toxicityLevel > 4 -> EcoWarning
                            else -> Color.Green
                        },
                        modifier = Modifier.size(8.dp)
                    ) {}
                }
            }
        }
    }
}

@Composable
fun EnhancedResultSection(
    correct: Boolean,
    correctAnswerText: String,
    educationalFact: String,
    detailedComparison: String,
    onContinue: () -> Unit
) {
    val backgroundColor = if (correct) Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
    val accentColor = if (correct) EcoSuccess else EcoError

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icona risultato con animazione
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (correct) 1.2f else 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Icon(
                imageVector = if (correct) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (correct) "Corretto! 🎉" else "Sbagliato! 😞",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Risposta corretta
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = correctAnswerText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = EcoDarkGreen,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comparazione dettagliata
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = detailedComparison,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fatto educativo
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoLightGreen.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = EcoWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lo sapevi?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = EcoDarkGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = educationalFact,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }

            if (correct) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continua",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedInstructionFooter() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = EcoCardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = EcoGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Tocca l'oggetto che pensi sia la risposta corretta!",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = EcoDarkGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EnhancedTutorialOverlay(onDismiss: () -> Unit) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable {
                if (currentStep < totalSteps) {
                    currentStep++
                } else {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icona per ogni step
                val icon = when(currentStep) {
                    1 -> Icons.Default.EmojiNature
                    2 -> Icons.Default.Psychology
                    3 -> Icons.Default.Speed
                    else -> Icons.Default.EmojiEvents
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EcoGreen,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when(currentStep) {
                        1 -> "Benvenuto a EcoGame!"
                        2 -> "Domande Avanzate"
                        3 -> "Sistema di Punteggio"
                        else -> "Pronto a iniziare?"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreen
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when(currentStep) {
                        1 -> "Metti alla prova le tue conoscenze ambientali! Confronta due oggetti e scegli quello che meglio risponde alla domanda."
                        2 -> "Le domande includono: tempo di decomposizione, impatto sul clima, livello di tossicità, facilità di riciclaggio e molto altro."
                        3 -> "Risposte consecutive corrette danno bonus combo! Più veloce rispondi, più punti guadagni. Livelli alti = meno tempo!"
                        else -> "Usa le tue conoscenze ambientali per vincere. Buona fortuna, eco-guerriero! 🌱"
                    },
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Indicatori di progresso
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..totalSteps) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (i <= currentStep) EcoGreen else Color.LightGray,
                            modifier = Modifier
                                .width(if (i <= currentStep) 24.dp else 12.dp)
                                .height(6.dp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (currentStep < totalSteps) "Continua" else "Inizia a giocare!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConfettiAnimation() {
    // Implementazione semplificata dell'animazione confetti
    val infiniteTransition = rememberInfiniteTransition()
    val confettiOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(10) { index ->
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = listOf(Color.Yellow, Color.Red, Color.Blue, Color.Green, EcoWarning).random(),
                modifier = Modifier
                    .offset(
                        x = (index * 40).dp + confettiOffset.dp,
                        y = (index * 60).dp + confettiOffset.dp
                    )
                    .size(24.dp)
                    .graphicsLayer(
                        rotationZ = confettiOffset * 2
                    )
            )
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    viewModel: EcoGameViewModel = viewModel()
) {
    var totalPoints by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.sendScoreToServer(
            onSuccess = { points ->
                totalPoints = points
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EcoBackground, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icona game over animata
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Icon(
                imageVector = if (score > 20) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                contentDescription = null,
                tint = if (score > 20) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Game Over",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = EcoGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Punteggio finale
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoLightGreen.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Punteggio Finale",
                        style = MaterialTheme.typography.titleMedium,
                        color = EcoDarkGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Motivo della sconfitta
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = viewModel.gameOverReason.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Punti totali
            if (isLoading) {
                CircularProgressIndicator(color = EcoGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aggiornamento punti...", color = Color.Gray)
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = EcoError,
                    textAlign = TextAlign.Center
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EcoLightGreen),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiNature,
                            contentDescription = null,
                            tint = EcoGreen,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Eco Points Totali",
                            style = MaterialTheme.typography.titleMedium,
                            color = EcoDarkGreen,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "$totalPoints",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pulsanti con stile migliorato
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gioca ancora",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Torna alla Home",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}