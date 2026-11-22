package com.ve.bookkeeping.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.BabyChangingStation
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CarRepair
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Commute
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.Icecream
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PedalBike
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Propane
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Stadium
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Tram
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** 一个可选图标：[id] 用于持久化，[vector] 为空表示需要联网下载。 */
data class AppIcon(
    val id: String,
    val label: String,
    val group: String,
    val vector: ImageVector? = null,
    val remoteUrl: String? = null,
)

/**
 * 内置图标库（0 额外包体积：全部复用已编译进来的 Material Rounded）。
 *
 * 这是「方案 C」的内置部分：断网也能用，保证不白屏。
 * 联网扩展图标见 [IconLibraryPage] 里的远程清单。
 */
object IconCatalog {

    private fun icon(id: String, label: String, group: String, vector: ImageVector) =
        AppIcon(id = id, label = label, group = group, vector = vector)

    val builtIn: List<AppIcon> = listOf(
        // 餐饮
        icon("restaurant", "餐饮", "餐饮", Icons.Rounded.Restaurant),
        icon("fastfood", "快餐", "餐饮", Icons.Rounded.Fastfood),
        icon("local_cafe", "咖啡", "餐饮", Icons.Rounded.LocalCafe),
        icon("coffee", "咖啡杯", "餐饮", Icons.Rounded.Coffee),
        icon("cake", "甜点", "餐饮", Icons.Rounded.Cake),
        icon("icecream", "冰淇淋", "餐饮", Icons.Rounded.Icecream),
        icon("bakery", "面包", "餐饮", Icons.Rounded.BakeryDining),
        icon("wine_bar", "酒吧", "餐饮", Icons.Rounded.WineBar),
        icon("egg", "生鲜", "餐饮", Icons.Rounded.Egg),
        icon("grocery", "超市", "餐饮", Icons.Rounded.LocalGroceryStore),
        // 交通
        icon("commute", "通勤", "交通", Icons.Rounded.Commute),
        icon("bus", "公交", "交通", Icons.Rounded.DirectionsBus),
        icon("train", "地铁", "交通", Icons.Rounded.Train),
        icon("tram", "轻轨", "交通", Icons.Rounded.Tram),
        icon("taxi", "打车", "交通", Icons.Rounded.LocalTaxi),
        icon("car", "自驾", "交通", Icons.Rounded.DirectionsCar),
        icon("car_repair", "汽车养护", "交通", Icons.Rounded.CarRepair),
        icon("gas", "加油", "交通", Icons.Rounded.LocalGasStation),
        icon("bike", "骑行", "交通", Icons.Rounded.PedalBike),
        icon("flight", "机票", "交通", Icons.Rounded.Flight),
        // 购物
        icon("shopping_bag", "购物", "购物", Icons.Rounded.ShoppingBag),
        icon("shopping_cart", "日用品", "购物", Icons.Rounded.ShoppingCart),
        icon("checkroom", "衣物", "购物", Icons.Rounded.Checkroom),
        icon("redeem", "礼物", "购物", Icons.Rounded.Redeem),
        icon("card_giftcard", "礼品卡", "购物", Icons.Rounded.CardGiftcard),
        icon("brush", "美妆", "购物", Icons.Rounded.Brush),
        icon("content_cut", "理发", "购物", Icons.Rounded.ContentCut),
        // 居住
        icon("home", "房租", "居住", Icons.Rounded.Home),
        icon("apartment", "物业", "居住", Icons.Rounded.Apartment),
        icon("bolt", "电费", "居住", Icons.Rounded.Bolt),
        icon("electric_bolt", "电费2", "居住", Icons.Rounded.ElectricBolt),
        icon("water_drop", "水费", "居住", Icons.Rounded.WaterDrop),
        icon("gas_station", "燃气", "居住", Icons.Rounded.Propane),
        icon("wifi", "宽带", "居住", Icons.Rounded.Wifi),
        icon("router", "宽带2", "居住", Icons.Rounded.Router),
        icon("hotel", "住宿", "居住", Icons.Rounded.Hotel),
        icon("umbrella", "保险", "居住", Icons.Rounded.Umbrella),
        // 医疗
        icon("hospital", "医院", "医疗", Icons.Rounded.LocalHospital),
        icon("pharmacy", "药店", "医疗", Icons.Rounded.LocalPharmacy),
        icon("medication", "药品", "医疗", Icons.Rounded.Medication),
        icon("health", "体检", "医疗", Icons.Rounded.HealthAndSafety),
        // 教育
        icon("school", "学费", "教育", Icons.Rounded.School),
        icon("book", "书籍", "教育", Icons.Rounded.Book),
        icon("menu_book", "课程", "教育", Icons.Rounded.MenuBook),
        icon("science", "实验", "教育", Icons.Rounded.Science),
        // 娱乐
        icon("movie", "电影", "娱乐", Icons.Rounded.Movie),
        icon("music", "音乐", "娱乐", Icons.Rounded.MusicNote),
        icon("piano", "乐器", "娱乐", Icons.Rounded.Piano),
        icon("sports_esports", "游戏", "娱乐", Icons.Rounded.SportsEsports),
        icon("celebration", "聚会", "娱乐", Icons.Rounded.Celebration),
        icon("event", "活动", "娱乐", Icons.Rounded.Event),
        icon("photo_camera", "摄影", "娱乐", Icons.Rounded.PhotoCamera),
        icon("headphones", "耳机", "娱乐", Icons.Rounded.Headphones),
        // 健康 / 运动
        icon("fitness", "健身", "健康", Icons.Rounded.FitnessCenter),
        icon("sports_soccer", "球类", "健康", Icons.Rounded.SportsSoccer),
        icon("stadium", "赛事", "健康", Icons.Rounded.Stadium),
        icon("spa", "按摩", "健康", Icons.Rounded.Spa),
        icon("park", "公园", "健康", Icons.Rounded.Park),
        icon("forest", "户外", "健康", Icons.Rounded.Forest),
        // 其他
        icon("pets", "宠物", "其他", Icons.Rounded.Pets),
        icon("baby", "母婴", "其他", Icons.Rounded.ChildCare),
        icon("baby_station", "婴儿", "其他", Icons.Rounded.BabyChangingStation),
        icon("work", "工作", "其他", Icons.Rounded.Work),
        icon("computer", "数码", "其他", Icons.Rounded.Computer),
        icon("phone", "话费", "其他", Icons.Rounded.PhoneIphone),
        icon("tv", "影音", "其他", Icons.Rounded.Tv),
        icon("subscriptions", "订阅", "其他", Icons.Rounded.Subscriptions),
        icon("credit_card", "还款", "其他", Icons.Rounded.CreditCard),
        icon("payments", "手续费", "其他", Icons.Rounded.Payments),
        icon("attach_money", "收入", "其他", Icons.Rounded.AttachMoney),
        icon("savings", "储蓄", "其他", Icons.Rounded.Savings),
        icon("card", "银行卡", "其他", Icons.Rounded.CreditCard),
        icon("gavel", "罚款", "其他", Icons.Rounded.Gavel),
        icon("handyman", "维修", "其他", Icons.Rounded.Handyman),
        icon("build", "工具", "其他", Icons.Rounded.Build),
        icon("smoking", "烟酒", "其他", Icons.Rounded.SmokingRooms),
        icon("diversity", "社交", "其他", Icons.Rounded.Diversity3),
        icon("face", "宠物3", "其他", Icons.Rounded.Face),
        icon("favorite", "爱心", "其他", Icons.Rounded.Favorite),
        icon("emoji_events", "奖励", "其他", Icons.Rounded.EmojiEvents),
        icon("lock", "保险柜", "其他", Icons.Rounded.Lock),
        icon("edit", "其他杂项", "其他", Icons.Rounded.Edit),
        icon("category", "其他", "其他", Icons.Rounded.Category),
    )

    val groups: List<String> get() = builtIn.map { it.group }.distinct()

    fun find(id: String?): AppIcon? = builtIn.firstOrNull { it.id == id }
}
