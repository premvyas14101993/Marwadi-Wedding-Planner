package com.example.data.local

import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.GiftEntity
import com.example.data.local.entities.GuestEntity
import com.example.data.local.entities.MaterialEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.PersonEntity
import com.example.data.local.entities.RitualChecklistItemEntity
import com.example.data.local.entities.RitualEntity
import com.example.data.local.entities.TaskEntity
import com.example.data.local.entities.VendorEntity
import com.example.data.local.entities.VendorQuotationEntity
import com.example.data.local.entities.WeddingEntity

data class DefaultRitualTemplate(
    val name: String,
    val hindiName: String,
    val description: String,
    val culturalSignificance: String,
    val vidhiDetails: String,
    val defaultChecklist: List<String>,
    val defaultMaterials: List<DefaultMaterialTemplate>
)

data class DefaultMaterialTemplate(
    val item: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val estimatedCost: Double
)

object DefaultTemplates {

    val MARWADI_RITUALS = listOf(
        DefaultRitualTemplate(
            name = "Ganesh Sthapana",
            hindiName = "गणेश स्थापना",
            description = "The auspicious invocation of Lord Ganesha to remove all obstacles and bless the wedding festivities.",
            culturalSignificance = "Lord Ganesha is Vighnaharta (remover of obstacles). No auspicious ceremony begins without invoking His blessings.",
            vidhiDetails = "Pandit ji conducts the puja with turmeric Ganesha or clay idol, lighting diya, offering modaks, dub grass, supari, and sacred thread (Janeu).",
            defaultChecklist = listOf(
                "Arrange Pandit Ji and fix muhurat",
                "Clean and decorate the puja altar with rangoli",
                "Prepare Kalash with mango leaves and coconut",
                "Arrange fresh flowers, dub grass, and Panchamrit",
                "Distribute Prasad to family members"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Lord Ganesha Idol / Supari", "Puja Samagri", 1.0, "pcs", 500.0),
                DefaultMaterialTemplate("Puja Kalash (Copper/Brass)", "Puja Samagri", 2.0, "pcs", 800.0),
                DefaultMaterialTemplate("Coconut with water (Nariyal)", "Puja Samagri", 5.0, "pcs", 250.0),
                DefaultMaterialTemplate("Mango Leaves (Aam ke Patte)", "Puja Samagri", 21.0, "pcs", 100.0),
                DefaultMaterialTemplate("Dub Grass (Durva)", "Puja Samagri", 1.0, "bunch", 50.0),
                DefaultMaterialTemplate("Kumkum, Sindoor & Akshat", "Puja Samagri", 1.0, "set", 200.0),
                DefaultMaterialTemplate("Modak / Besan Ladoo (Prasad)", "Sweets & Dryfruits", 2.0, "kg", 1200.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Roka",
            hindiName = "रोका समारोह",
            description = "Formal agreement and announcement securing the union between the two families.",
            culturalSignificance = "Symbolizes that the search for partners is complete (Roka means to stop/secure).",
            vidhiDetails = "Elders from both sides exchange sweets, dry fruits, and cash shagun. Tilak is applied on the groom and bride.",
            defaultChecklist = listOf(
                "Finalize guest list of close relatives",
                "Order custom sweet boxes and dry fruit platters",
                "Arrange Shagun envelopes (Lifafa)",
                "Book intimate venue/hall and photographer"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Dry Fruit Gift Hampers", "Gifts", 11.0, "boxes", 8500.0),
                DefaultMaterialTemplate("Shagun Envelopes (Lifafas)", "Puja Samagri", 21.0, "pcs", 300.0),
                DefaultMaterialTemplate("Mawa Sweets / Kaju Katli", "Sweets & Dryfruits", 5.0, "kg", 4500.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Sagai (Engagement)",
            hindiName = "सगाई / मुद्दत",
            description = "Traditional Marwadi engagement ceremony with exchange of rings and formal blessings.",
            culturalSignificance = "Formal exchange of vows and rings in the presence of extended community.",
            vidhiDetails = "Bride's family welcomes groom's family with garland and Tikka. Rings are exchanged followed by feast.",
            defaultChecklist = listOf(
                "Finalize engagement rings and safe storage",
                "Coordinate ring platter and stage decor",
                "Prepare welcome garlands and ittar (perfume) spray",
                "Finalize catering menu and dessert counters"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Engagement Rings Platter", "Decoration", 1.0, "pcs", 2000.0),
                DefaultMaterialTemplate("Rose Garlands (Varmala)", "Decoration", 4.0, "pcs", 1200.0),
                DefaultMaterialTemplate("Traditional Ittar & Rose Water", "Puja Samagri", 2.0, "bottles", 800.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Tilak & Godh Bharai",
            hindiName = "तिलक एवं गोद भराई",
            description = "Bride's family applies Tilak to the groom, and groom's family fills the bride's lap with auspicious gifts.",
            culturalSignificance = "Welcoming the bride into the new lineage with prosperity, jewellery, and auspicious coconuts.",
            vidhiDetails = "Father and brothers apply saffron Tilak and present sword/pen and clothing. Bride receives Chunari and dry fruits.",
            defaultChecklist = listOf(
                "Arrange silver coin / gold coin for Tilak",
                "Prepare traditional Odhna / Chunari for bride",
                "Prepare dry fruit potlis and coconut with silver foil",
                "Assemble traditional Poshak garments"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Silver Tilak Thali Set", "Puja Samagri", 1.0, "set", 3500.0),
                DefaultMaterialTemplate("Bandhani Chunari / Pila Odhna", "Clothing", 1.0, "pcs", 4500.0),
                DefaultMaterialTemplate("Supari & Silver Coin Shagun", "Gifts", 11.0, "sets", 5500.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Baan",
            hindiName = "बान बैठना",
            description = "Start of the holy bathing and cleansing rituals for the bride and groom at their respective homes.",
            culturalSignificance = "Marking the confinement of the bride and groom within home boundaries for protection from evil eye.",
            vidhiDetails = "Married women apply ubtan with grass stalks dipped in mustard oil and curd while singing traditional geet.",
            defaultChecklist = listOf(
                "Prepare wooden Chauki (Patta) with auspicious drawings",
                "Prepare Brass Thali with ubtan paste and raw milk",
                "Sing traditional Marwadi Baan geet with Dholak",
                "Tie Kangana (protective sacred thread) on wrist"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Chauki / Patta with Red Cloth", "Puja Samagri", 2.0, "pcs", 1200.0),
                DefaultMaterialTemplate("Kangana (Protective Thread & Shells)", "Puja Samagri", 4.0, "pcs", 400.0),
                DefaultMaterialTemplate("Pure Mustard Oil & Curd", "Puja Samagri", 2.0, "liters", 350.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Haldi",
            hindiName = "हल्दी रस्म",
            description = "Application of fragrant turmeric and sandalwood paste to enhance glow and ward off negativity.",
            culturalSignificance = "Turmeric is an antiseptic, beautifying agent and sacred purifier.",
            vidhiDetails = "Family members lovingly smear fresh turmeric paste with marigold petals amidst music and playful laughter.",
            defaultChecklist = listOf(
                "Arrange organic fresh Kasturi Haldi paste with rose water",
                "Setup yellow-themed decor with Marigold flower backdrop",
                "Yellow dress code coordination for family and guests",
                "Hire dhol players and photography props"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Organic Kasturi Turmeric Powder", "Puja Samagri", 2.0, "kg", 800.0),
                DefaultMaterialTemplate("Gulab Jal (Pure Rose Water)", "Puja Samagri", 3.0, "bottles", 450.0),
                DefaultMaterialTemplate("Yellow Marigold Flowers & Petals", "Decoration", 20.0, "kg", 3000.0),
                DefaultMaterialTemplate("Floral Jewellery for Bride", "Clothing", 1.0, "set", 2500.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Mehendi",
            hindiName = "मेहंदी की रात",
            description = "Application of intricate henna patterns on bride's and female guests' hands and feet.",
            culturalSignificance = "Deep color of henna symbolizes deep love between the couple and longevity of marriage.",
            vidhiDetails = "Professional Mehendi artists draw Radha-Krishna, Doli, and Baraat motifs while folk songs resonate.",
            defaultChecklist = listOf(
                "Book top Rajasthani Mehendi artists",
                "Prepare relaxing seating with colorful cushions and bolsters",
                "Arrange lemon-sugar syrup and clove smoke for dark stain",
                "Snack platters, chaat counters, and mocktails"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Special Rajasthani Sojat Mehendi Cones", "Puja Samagri", 50.0, "pcs", 1500.0),
                DefaultMaterialTemplate("Nilgiri & Clove Oil for Mehendi", "Puja Samagri", 2.0, "bottles", 400.0),
                DefaultMaterialTemplate("Bangles & Bindi Favors for Guests", "Gifts", 100.0, "sets", 5000.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Pithi Dastoor",
            hindiName = "पीठी दस्तूर",
            description = "Special Marwadi tradition of applying aromatic paste of sandalwood, chickpea flour, and turmeric.",
            culturalSignificance = "Ancient royal tradition ensuring bridal glow and blessing from elder married women.",
            vidhiDetails = "Bride/Groom wears yellow poshak, enters under a silk canopy (Chadar) held by four cousins.",
            defaultChecklist = listOf(
                "Arrange yellow Phulkari / Silk Chadar",
                "Prepare special Pithi paste (Besan, Kesar, Chandan, Haldi)",
                "Distribute traditional sweets like Ghevar and Peda"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Pure Kashmiri Saffron (Kesar)", "Puja Samagri", 5.0, "gm", 1500.0),
                DefaultMaterialTemplate("Sandalwood Powder (Chandan)", "Puja Samagri", 500.0, "gm", 1200.0),
                DefaultMaterialTemplate("Rajasthani Ghevar / Mawa Peda", "Sweets & Dryfruits", 5.0, "kg", 4000.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Grah Shanti",
            hindiName = "ग्रह शांति हवन",
            description = "Vedic fire ritual (Havan) to harmonize all nine astrological planets (Navagraha).",
            culturalSignificance = "Removes any doshas in the horoscope and invites cosmic alignment for happy married life.",
            vidhiDetails = "Pandits chant Vedic mantras and offer Navagraha samidha, ghee, sesame seeds, and barley into the holy agni.",
            defaultChecklist = listOf(
                "Arrange Havan Kund, bricks, and sand platform",
                "Order Navagraha cloth pieces (9 colors) and 9 grains",
                "Arrange pure desi cow ghee and dry coconut halves (Gola)",
                "Coordinate Dakshina for pandits and helpers"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Pure Desi Cow Ghee", "Puja Samagri", 5.0, "kg", 3500.0),
                DefaultMaterialTemplate("Havan Samagri & Samidha Sticks", "Puja Samagri", 5.0, "kg", 1800.0),
                DefaultMaterialTemplate("Navagraha 9 Colored Cloth & Grains", "Puja Samagri", 1.0, "set", 750.0),
                DefaultMaterialTemplate("Dry Coconut Halves (Sukha Gola)", "Puja Samagri", 11.0, "pcs", 650.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Mata Pujan",
            hindiName = "माता पूजन / कुलदेवी पूजा",
            description = "Worship of the Kuldevi (family ancestral deity) seeking eternal maternal protection.",
            culturalSignificance = "Honor to family roots and ancestors who guide future generations.",
            vidhiDetails = "Elders offer red Chunari, bangles, Kajal, Haldi, and special home-cooked Prasad (Lapsi & Puri).",
            defaultChecklist = listOf(
                "Prepare traditional Lapsi and Puri Prasad",
                "Arrange Solah Shringar items for Kuldevi offering",
                "Organize family gathering and elder blessings"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Solah Shringar Offering Set for Mata", "Puja Samagri", 2.0, "sets", 1600.0),
                DefaultMaterialTemplate("Red Silk Chunari with Gota Patti", "Puja Samagri", 2.0, "pcs", 900.0),
                DefaultMaterialTemplate("Desi Ghee Lapsi Ingredients", "Sweets & Dryfruits", 1.0, "set", 800.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Mandap Muhurat",
            hindiName = "मंडप मुहूर्त",
            description = "Sanctifying the wedding mandap pillars and laying the foundation for sacred vows.",
            culturalSignificance = "The Mandap represents the universe, and the four pillars represent parents and four virtues.",
            vidhiDetails = "Pandit ji worships the first bamboo/wood pillar (Thambha) with turmeric, kumkum, and mango leaves.",
            defaultChecklist = listOf(
                "Coordinate with decorator for traditional wooden pillars",
                "Prepare Kalash, betel nuts, and sacred thread",
                "Distribute Prasad to workers and family"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Sacred Pillar Wood / Bamboo Pole", "Decoration", 4.0, "pcs", 2000.0),
                DefaultMaterialTemplate("Red Cloth (Khaadi / Silk) for Mandap", "Decoration", 10.0, "meters", 1500.0),
                DefaultMaterialTemplate("Mauli / Kalava Threads", "Puja Samagri", 10.0, "rolls", 250.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Tel Baan",
            hindiName = "तेल बान",
            description = "Auspicious application of mustard oil by married women using grass brushes.",
            culturalSignificance = "Purification and emotional bonding with female relatives.",
            vidhiDetails = "Mothers, aunts, and sisters dip grass brooms into mustard oil and apply it to head, shoulders, knees, and feet.",
            defaultChecklist = listOf(
                "Prepare mustard oil bowls with fresh dub grass bundles",
                "Dress in traditional Bandhej or Leheriya attire",
                "Record festive singing and laughter moments"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Kachi Ghani Mustard Oil", "Puja Samagri", 2.0, "liters", 400.0),
                DefaultMaterialTemplate("Kusha Grass Whisk (Chaur)", "Puja Samagri", 4.0, "pcs", 300.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Mehfil",
            hindiName = "शाही महफ़िल",
            description = "Royal Rajasthani musical evening with Ghoomar dance and traditional folk performances.",
            culturalSignificance = "A celebration of Marwadi heritage with authentic court dances, poetry, and hospitality.",
            vidhiDetails = "Ladies gather in royal attire for Ghoomar dance; gentlemen celebrate with classical instruments and Shehnai.",
            defaultChecklist = listOf(
                "Book Manganiyar/Langa Rajasthani folk artists",
                "Coordinate royal Diwan seating with low tables and carpets",
                "Arrange Ghoomar costumes and authentic snacks (Pyaaz Kachori, Mirchi Vada)"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Royal Low Seating Cushions & Gaddi", "Decoration", 1.0, "setup", 15000.0),
                DefaultMaterialTemplate("Traditional Rajasthani Snacks Platters", "Food & Catering", 1.0, "lot", 12000.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Sangeet",
            hindiName = "संगीत संध्या",
            description = "High-energy musical dance night with choreographed family performances and DJ.",
            culturalSignificance = "Brings both sides together through joyous song, dance, friendly banter, and bonding.",
            vidhiDetails = "Choreographed performances by cousins, parents, and couple followed by dance floor opening.",
            defaultChecklist = listOf(
                "Book sound system, LED wall, and stage lighting",
                "Coordinate song playlists and couple entry track",
                "Finalize dance choreography rehearsals",
                "Arrange midnight snacks and refreshments"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Stage Lighting, Cold Pyro & Fog Setup", "Entertainment & Music", 1.0, "set", 35000.0),
                DefaultMaterialTemplate("Custom DJ Playlist & Sound Engineering", "Entertainment & Music", 1.0, "booking", 25000.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Bhaat (Mayra)",
            hindiName = "भात (मायरा)",
            description = "Grand welcoming of maternal uncles (Mama Ji) who bring lavish gifts and poshak for the entire family.",
            culturalSignificance = "One of the most emotional and pivotal rituals celebrating maternal brother-sister devotion.",
            vidhiDetails = "Sister welcomes brother at the threshold with Tilak and brass plate. Maternal family showers gold, silver, clothes, and cash.",
            defaultChecklist = listOf(
                "Prepare royal welcome gate with Shehnai and Dhol",
                "Prepare traditional Aarti Thali and sweet jaggery (Gud)",
                "Assemble Poshak garments for all paternal family members",
                "Arrange photographer for Mama Ji arrival moment"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Silver Welcome Thali with Ghee Diya", "Puja Samagri", 1.0, "pcs", 4000.0),
                DefaultMaterialTemplate("Traditional Poshak & Safa Sets for Family", "Clothing", 25.0, "sets", 75000.0),
                DefaultMaterialTemplate("Dry Fruit & Sweet Hampers for Mayra", "Gifts", 21.0, "boxes", 18000.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Nikasi",
            hindiName = "निकासी",
            description = "Groom's royal departure ceremony where he wears the royal Safa, Sehra, and mounts a decorated mare (Ghodi).",
            culturalSignificance = "Symbolizes the groom stepping out as a noble prince ready to claim his bride.",
            vidhiDetails = "Sisters-in-law apply Kajal, sisters tie golden Sehra and feed lentils (Chana Dal) to the mare.",
            defaultChecklist = listOf(
                "Arrange Royal Rajasthani Safa (Turban) tying artist",
                "Procure Royal Sword (Talwar) and pearl Sehra",
                "Book decorated white mare (Ghodi) and vintage open car",
                "Arrange cash currency notes for Aarti varna (Nazar)"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Royal Silk Safa (Pachrangi / Jodhpuri)", "Clothing", 1.0, "pcs", 3500.0),
                DefaultMaterialTemplate("Pearl & Stone Studded Sehra / Kalgi", "Clothing", 1.0, "pcs", 4500.0),
                DefaultMaterialTemplate("Traditional Ceremonial Sword (Talwar)", "Clothing", 1.0, "pcs", 2500.0),
                DefaultMaterialTemplate("Chana Dal & Gur for Ghodi Pujan", "Puja Samagri", 1.0, "kg", 150.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Barat Procession",
            hindiName = "बारात शोभायात्रा",
            description = "Grand procession of groom, family, and friends dancing with brass band, mobile chandeliers, and fireworks.",
            culturalSignificance = "Public joyful proclamation of the wedding celebration.",
            vidhiDetails = "Baratis dance enthusiastically to brass band tunes, fireworks illuminate the sky, reaching the wedding venue.",
            defaultChecklist = listOf(
                "Coordinate Brass Band, Dholis, and mobile sound trolley",
                "Ensure police permission / route clearance if on main road",
                "Distribute matching royal Safas (Turbans) to all Baratis",
                "Arrange cold water bottles and wet wipes for dancing guests"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Barati Safas (Turbans with Brooch)", "Clothing", 50.0, "pcs", 20000.0),
                DefaultMaterialTemplate("Safe Cold Pyro / Confetti Shooters", "Entertainment & Music", 10.0, "pcs", 4500.0),
                DefaultMaterialTemplate("Hydration Kits & Cold Drinks for Procession", "Food & Catering", 1.0, "lot", 3500.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Toran",
            hindiName = "तोरण मारना",
            description = "Groom touches the holy Toran suspended at the venue threshold using his ceremonial sword or neem twig.",
            culturalSignificance = "Warding off any negative energy or arrogance before entering the bride's sanctuary.",
            vidhiDetails = "Mother-in-law performs Aarti, pulls groom's nose playfully, and gives blessings.",
            defaultChecklist = listOf(
                "Hang auspicious handcrafted Toran at the main entry gate",
                "Prepare welcoming Aarti Thali with flour lamps (Aata Diya)",
                "Coordinate welcome drinks and ittar for arriving Baratis"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Handcrafted Auspicious Toran", "Decoration", 1.0, "pcs", 1500.0),
                DefaultMaterialTemplate("Aarti Welcome Thali with Camphor & Diya", "Puja Samagri", 2.0, "sets", 1200.0),
                DefaultMaterialTemplate("Rose Petals for Showering Groom", "Decoration", 10.0, "kg", 1500.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Jaimala (Varmala)",
            hindiName = "जयमाला (वरमाला)",
            description = "Exquisite exchange of floral garlands between bride and groom upon the grand stage.",
            culturalSignificance = "Mutual acceptance of each other as life partners in the presence of all witnesses.",
            vidhiDetails = "Bride makes a royal entry under floral canopy; couple exchanges garlands amidst fireworks and rose petals.",
            defaultChecklist = listOf(
                "Order fresh exotic rose & orchid Varmalas",
                "Coordinate grand bridal entry theme and song",
                "Setup hydraulic revolving stage or flower shower machine",
                "Stage photography and live video recording"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Designer Fresh Rose & Orchid Varmalas", "Decoration", 2.0, "pcs", 4000.0),
                DefaultMaterialTemplate("Floral Canopy (Phoolon ki Chaadar) for Bride", "Decoration", 1.0, "pcs", 3500.0),
                DefaultMaterialTemplate("Rose Petal Cannons for Varmala Moment", "Entertainment & Music", 4.0, "pcs", 2800.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Kanyadaan",
            hindiName = "कन्यादान",
            description = "Sacred and poignant offering of the bride's hand to the groom by her parents.",
            culturalSignificance = "Considered the greatest mahadaan in Hindu Vedic tradition, seeking divine spiritual merit.",
            vidhiDetails = "Father places daughter's hand in groom's right hand with Ganga Jal, betel leaf, gold coin, and sacred chants.",
            defaultChecklist = listOf(
                "Keep Ganga Jal, sacred Kusha grass, and betel leaves ready",
                "Keep silver coin or gold piece ready in parents' hands",
                "Ensure quiet and solemn atmosphere for Vedic chanting"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Holy Ganga Jal in Brass Lota", "Puja Samagri", 1.0, "lota", 300.0),
                DefaultMaterialTemplate("Paan Betel Leaves & Supari", "Puja Samagri", 21.0, "pcs", 200.0),
                DefaultMaterialTemplate("Silver / Gold Coin for Kanyadaan", "Puja Samagri", 1.0, "pcs", 3000.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Hast Milap (Gathbandhan)",
            hindiName = "हस्त मिलाप एवं गठबंधन",
            description = "Tying the sacred knot connecting the groom's scarf (Patka) to the bride's veil (Chunari).",
            culturalSignificance = "Unbreakable bond of unity, mutual support, and joint destiny.",
            vidhiDetails = "Groom's sister places 5 auspicious items (coin, rice, turmeric piece, flower, supari) into the knot and ties it.",
            defaultChecklist = listOf(
                "Prepare silk Gathbandhan Dupatta / Patka",
                "Prepare 5 auspicious items for sister's ritual",
                "Sister's Shagun gift envelope ready"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Embroidered Silk Gathbandhan Cloth", "Puja Samagri", 1.0, "pcs", 1800.0),
                DefaultMaterialTemplate("Whole Turmeric Pieces (Haldi Ganth)", "Puja Samagri", 5.0, "pcs", 100.0),
                DefaultMaterialTemplate("Shagun Gift for Groom's Sister", "Gifts", 1.0, "set", 5000.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Pheras (Saptapadi)",
            hindiName = "सात फेरे (सप्तपदी)",
            description = "The seven sacred circumambulations around the sacred fire taking eternal marital vows.",
            culturalSignificance = "The core Vedic ritual where the marriage is formally and spiritually sealed.",
            vidhiDetails = "First 4 pheras led by groom (Dharma, Artha, Kama), last 3 led by bride (Moksha & devotion).",
            defaultChecklist = listOf(
                "Arrange pure Havan samagri, camphor, and wood for Agni",
                "Keep Puffed Rice (Kheel / Laja) ready for brothers' ritual (Laja Homam)",
                "Read out and translate seven vows for couple understanding",
                "Keep flower petals ready for elders to shower at 7th phera"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Kheel / Puffed Rice for Laja Homam", "Puja Samagri", 2.0, "kg", 250.0),
                DefaultMaterialTemplate("Dry Mango Wood (Aam ki Lakdi) for Mandap Agni", "Puja Samagri", 10.0, "kg", 800.0),
                DefaultMaterialTemplate("Camphor (Karpur) Tablets", "Puja Samagri", 500.0, "gm", 450.0),
                DefaultMaterialTemplate("Fresh Rose & Jasmine Petals for Blessings", "Decoration", 15.0, "kg", 2500.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Sindoor Daan & Mangalsutra",
            hindiName = "सिंदूर दान एवं मंगलसूत्र",
            description = "Groom applies vermilion (Sindoor) to bride's hair parting and ties the sacred golden Mangalsutra.",
            culturalSignificance = "Marks the transition of the bride to Soubhagyavati (a blessed married woman).",
            vidhiDetails = "Groom uses a silver coin or betel leaf to apply sindoor concealed under a silk cloth, then fastens Mangalsutra.",
            defaultChecklist = listOf(
                "Keep consecrated Sindoor and pure silver coin ready",
                "Ensure Mangalsutra is blessed by Pandit and ready",
                "Keep mirror ready for the bride to see her reflection"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Pure Herbal Consecrated Sindoor", "Puja Samagri", 1.0, "box", 250.0),
                DefaultMaterialTemplate("Handcrafted Silver Mirror (Darpan)", "Puja Samagri", 1.0, "pcs", 1500.0),
                DefaultMaterialTemplate("Mangalsutra Safe Velvet Box", "Jewellery", 1.0, "pcs", 500.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Vidaai",
            hindiName = "विदाई",
            description = "Tearful and emotional farewell ceremony as the bride leaves her parental home for her new family.",
            culturalSignificance = "Bride throws handfuls of rice backward over her head repaying debt to her parents and wishing prosperity.",
            vidhiDetails = "Family members shower blessings, parents embrace daughter, car departs with coconut smashed for safe voyage.",
            defaultChecklist = listOf(
                "Prepare bowl of raw rice for the bride's backward throw",
                "Decorate Vidaai departure car with flowers",
                "Pack all bridal luggage and documents safely",
                "Comfort parents and close relatives"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Clean Raw Rice (Akshat) for Vidaai", "Puja Samagri", 2.0, "kg", 200.0),
                DefaultMaterialTemplate("Water Coconut for Car Departure Blessing", "Puja Samagri", 2.0, "pcs", 100.0),
                DefaultMaterialTemplate("Luxury Floral Car Decoration", "Decoration", 1.0, "service", 5000.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Griha Pravesh",
            hindiName = "गृह प्रवेश",
            description = "Joyous welcome of the newly married bride into her new home.",
            culturalSignificance = "Bride enters as Goddess Lakshmi bringing abundance and fortune.",
            vidhiDetails = "Mother-in-law performs Aarti at threshold; bride gently tilts a rice-filled brass Kalash with her right foot and walks on red dye (Aalta).",
            defaultChecklist = listOf(
                "Setup brass Kalash filled with rice at main entrance",
                "Prepare red Aalta dye plate and white sheet for auspicious footprints",
                "Organize fun ice-breaking games (finding ring in milk-rose bowl)"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Aalta (Red Auspicious Footprint Dye)", "Puja Samagri", 2.0, "bottles", 300.0),
                DefaultMaterialTemplate("White Cotton Fabric for Sacred Footsteps", "Puja Samagri", 5.0, "meters", 600.0),
                DefaultMaterialTemplate("Large Brass / Silver Bowl for Ring Game", "Puja Samagri", 1.0, "pcs", 2500.0),
                DefaultMaterialTemplate("Raw Cow Milk, Rose Petals & Silver Coin", "Puja Samagri", 1.0, "set", 400.0)
            )
        ),
        DefaultRitualTemplate(
            name = "Reception",
            hindiName = "प्रतिभोज (रिसेप्शन)",
            description = "Grand formal banquet and social gathering honoring the newly married couple.",
            culturalSignificance = "Formal introduction of the couple to extended community and dignitaries.",
            vidhiDetails = "Couple sits on grand stage receiving congratulations, gifts, photographs, followed by lavish feast.",
            defaultChecklist = listOf(
                "Finalize grand stage backdrop and seating sofa",
                "Coordinate 50+ item multi-cuisine royal banquet menu",
                "Manage guest gift registry and return gift counter",
                "Photographer and drone coverage"
            ),
            defaultMaterials = listOf(
                DefaultMaterialTemplate("Grand Stage Floral & Crystal Setup", "Decoration", 1.0, "service", 75000.0),
                DefaultMaterialTemplate("Royal Sweet & Mithai Gift Boxes for Guests", "Gifts", 200.0, "boxes", 70000.0),
                DefaultMaterialTemplate("Guest Welcome Drink & Mocktail Stations", "Food & Catering", 1.0, "service", 25000.0)
            )
        )
    )

    fun createDemoWedding(): WeddingEntity {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        val weddingDate = now + (95L * dayMillis) // 95 days in future
        return WeddingEntity(
            name = "Pankaj & Shubhangi Wedding",
            brideName = "Shubhangi Maheshwari",
            groomName = "Pankaj Vyas",
            weddingDate = weddingDate,
            engagementDate = now - (30L * dayMillis),
            venue = "Indana Palace, Jodhpur",
            city = "Jodhpur, Rajasthan",
            familyName = "Vyas & Maheshwari Pariwar",
            overallBudget = 2500000.0, // 25 Lakhs
            notes = "Grand Traditional Marwadi wedding celebration with full 26 traditional vidhis."
        )
    }

    fun createDemoExpenses(weddingId: Long): List<ExpenseEntity> {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        return listOf(
            ExpenseEntity(
                weddingId = weddingId,
                expenseName = "Venue Advance Payment (Indana Palace)",
                amount = 450000.0,
                date = now - (20L * dayMillis),
                category = "Venue & Accommodation",
                paidBy = "Father (Shri Ramesh Vyas)",
                paymentMode = "Bank Transfer",
                billNumber = "IND-2026-981",
                notes = "Advance for 3 days booking including banquet halls"
            ),
            ExpenseEntity(
                weddingId = weddingId,
                expenseName = "Royal Catering Advance",
                amount = 250000.0,
                date = now - (15L * dayMillis),
                category = "Food & Catering",
                paidBy = "Pankaj (Groom)",
                paymentMode = "Bank Transfer",
                billNumber = "CAT-8812",
                notes = "Pure veg Rajasthani + Continental live counters"
            ),
            ExpenseEntity(
                weddingId = weddingId,
                expenseName = "Mandap & Floral Decor Token",
                amount = 120000.0,
                date = now - (10L * dayMillis),
                category = "Decoration & Mandap",
                paidBy = "Uncle Rajesh (Mama Ji)",
                paymentMode = "UPI",
                billNumber = "DEC-442",
                notes = "Yellow theme for Haldi + Royal Rajwada Mandap"
            ),
            ExpenseEntity(
                weddingId = weddingId,
                expenseName = "Bridal Lehengas & Poshak Purchase",
                amount = 185000.0,
                date = now - (8L * dayMillis),
                category = "Clothing & Poshak",
                paidBy = "Bride's Family",
                paymentMode = "Card",
                billNumber = "JOD-POSH-102",
                notes = "Authentic Gota Patti bridal poshak from Jodhpur"
            ),
            ExpenseEntity(
                weddingId = weddingId,
                expenseName = "Groom Sherwani & Royal Safa",
                amount = 65000.0,
                date = now - (5L * dayMillis),
                category = "Clothing & Poshak",
                paidBy = "Pankaj (Groom)",
                paymentMode = "UPI",
                billNumber = "SHE-331",
                notes = "Ivory sherwani with hand embroidery and pearl kalgi"
            ),
            ExpenseEntity(
                weddingId = weddingId,
                expenseName = "Gold Jewellery & Mangalsutra",
                amount = 320000.0,
                date = now - (4L * dayMillis),
                category = "Jewellery",
                paidBy = "Father (Shri Ramesh Vyas)",
                paymentMode = "Bank Transfer",
                billNumber = "JWL-994",
                notes = "Kundan necklace set, Aad, and 22K Gold Mangalsutra"
            ),
            ExpenseEntity(
                weddingId = weddingId,
                expenseName = "Puja Samagri & Dry Fruits (Sthapana & Mayra)",
                amount = 38000.0,
                date = now - (2L * dayMillis),
                category = "Ritual Materials (Samagri)",
                paidBy = "Uncle Rajesh (Mama Ji)",
                paymentMode = "Cash",
                billNumber = "PUJ-120",
                notes = "Pure saffron, ghee, silver coins, and dry fruit baskets"
            ),
            ExpenseEntity(
                weddingId = weddingId,
                expenseName = "Cinematography & Drone Advance",
                amount = 75000.0,
                date = now - (1L * dayMillis),
                category = "Photography & Video",
                paidBy = "Pankaj (Groom)",
                paymentMode = "UPI",
                billNumber = "PHO-771",
                notes = "Candid photography, teaser video, and live streaming"
            )
        )
    }

    fun createDemoVendors(weddingId: Long): List<VendorEntity> {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        return listOf(
            VendorEntity(
                weddingId = weddingId,
                name = "Indana Palace Heritage Hotel",
                serviceType = "Venue",
                contactNumber = "+91 98290 12345",
                address = "Opp. Military Airport, Jodhpur, Rajasthan",
                gstNumber = "08AABCI1234F1Z1",
                totalContractValue = 900000.0,
                advancePaid = 450000.0,
                dueDate = now + (60L * dayMillis),
                rating = 4.9f,
                notes = "Includes 40 deluxe rooms and two banquet lawns"
            ),
            VendorEntity(
                weddingId = weddingId,
                name = "Maharaja Royal Caterers",
                serviceType = "Caterer",
                contactNumber = "+91 94141 56789",
                address = "Sojati Gate, Jodhpur",
                gstNumber = "08AABCM5678G2Z2",
                totalContractValue = 550000.0,
                advancePaid = 250000.0,
                dueDate = now + (70L * dayMillis),
                rating = 4.8f,
                notes = "Dal Baati Churma, Ker Sangri, Gatte ki Sabzi, Malpua, Ghevar"
            ),
            VendorEntity(
                weddingId = weddingId,
                name = "Rajwada Events & Decor",
                serviceType = "Decorator",
                contactNumber = "+91 98292 99887",
                address = "Ratanada, Jodhpur",
                gstNumber = "08AABCR9988H3Z3",
                totalContractValue = 300000.0,
                advancePaid = 120000.0,
                dueDate = now + (65L * dayMillis),
                rating = 4.7f,
                notes = "Floral mandap, fairy lights, royal chandelier lounge"
            ),
            VendorEntity(
                weddingId = weddingId,
                name = "Lens & Lights Wedding Stories",
                serviceType = "Photographer",
                contactNumber = "+91 97840 44556",
                address = "Paota, Jodhpur",
                gstNumber = "08AABCL4455J4Z4",
                totalContractValue = 180000.0,
                advancePaid = 75000.0,
                dueDate = now + (80L * dayMillis),
                rating = 4.9f,
                notes = "Traditional + candid team with 2 drone operators"
            ),
            VendorEntity(
                weddingId = weddingId,
                name = "Pandit Vishnu Sharma Shastri",
                serviceType = "Pandit",
                contactNumber = "+91 94142 33221",
                address = "Brahmpuri, Jodhpur",
                gstNumber = "",
                totalContractValue = 31000.0,
                advancePaid = 11000.0,
                dueDate = now + (90L * dayMillis),
                rating = 5.0f,
                notes = "Expert in authentic Marwadi Vedic vidhi & lagna muhurats"
            ),
            VendorEntity(
                weddingId = weddingId,
                name = "Sojat Mehendi Studio by Sunita",
                serviceType = "Mehendi Artist",
                contactNumber = "+91 98280 66778",
                address = "Sardarpura, Jodhpur",
                gstNumber = "",
                totalContractValue = 25000.0,
                advancePaid = 10000.0,
                dueDate = now + (85L * dayMillis),
                rating = 4.8f,
                notes = "Bridal figure mehendi + 6 assistant artists for guests"
            )
        )
    }

    fun createDemoPeople(weddingId: Long): List<PersonEntity> {
        return listOf(
            PersonEntity(
                weddingId = weddingId,
                name = "Shri Ramesh Vyas",
                phone = "+91 94140 11223",
                familySide = "GROOM_SIDE",
                relationship = "Groom's Father",
                role = "Head of Family",
                responsibility = "Overall budget, venue contract, pandit coordination",
                assignedRituals = "Ganesh Sthapana, Tilak, Kanyadaan, Pheras"
            ),
            PersonEntity(
                weddingId = weddingId,
                name = "Smt. Sunita Vyas",
                phone = "+91 94140 11224",
                familySide = "GROOM_SIDE",
                relationship = "Groom's Mother",
                role = "Ceremony Coordinator",
                responsibility = "Pithhi, Haldi, Tel Baan, Griha Pravesh preparations",
                assignedRituals = "Baan, Haldi, Tel Baan, Mata Pujan, Griha Pravesh"
            ),
            PersonEntity(
                weddingId = weddingId,
                name = "Shri Rajesh Sharma (Mama Ji)",
                phone = "+91 98290 55667",
                familySide = "GROOM_SIDE",
                relationship = "Groom's Maternal Uncle (Mama)",
                role = "Mayra / Bhaat Leader",
                responsibility = "Bringing Mayra gifts, safa coordination, welcoming baratis",
                assignedRituals = "Bhaat (Mayra), Barat, Nikasi"
            ),
            PersonEntity(
                weddingId = weddingId,
                name = "Shri Kailash Maheshwari",
                phone = "+91 98291 77889",
                familySide = "BRIDE_SIDE",
                relationship = "Bride's Father",
                role = "Bride Family Head",
                responsibility = "Mandap, Jaimala stage, Kanyadaan vidhi",
                assignedRituals = "Tilak, Toran, Kanyadaan, Vidaai"
            ),
            PersonEntity(
                weddingId = weddingId,
                name = "Amit Vyas",
                phone = "+91 97840 99881",
                familySide = "GROOM_SIDE",
                relationship = "Groom's Brother",
                role = "Logistics & Sangeet",
                responsibility = "Transportation, DJ & Sound, Barati safa distribution",
                assignedRituals = "Sangeet, Barat, Nikasi"
            ),
            PersonEntity(
                weddingId = weddingId,
                name = "Pooja Maheshwari",
                phone = "+91 98281 22334",
                familySide = "BRIDE_SIDE",
                relationship = "Bride's Sister",
                role = "Bridal Assistant & Juta Chupai",
                responsibility = "Mehendi, bridal entry, Juta Chupai planning",
                assignedRituals = "Mehendi, Jaimala, Pheras"
            )
        )
    }

    fun createDemoGuests(weddingId: Long): List<GuestEntity> {
        return listOf(
            GuestEntity(
                weddingId = weddingId,
                name = "Shri Suresh Sharma & Family",
                familyName = "Sharma Pariwar",
                side = "GROOM_SIDE",
                phone = "+91 94141 88990",
                address = "Shastri Nagar, Jaipur",
                city = "Jaipur",
                rsvpStatus = "ACCEPTED",
                invitationSent = true,
                numberOfMembers = 4,
                attendanceConfirmed = true,
                accommodationRequired = true,
                hotelRoomAllocated = "Room 201 (Indana)",
                foodPreference = "Pure Veg"
            ),
            GuestEntity(
                weddingId = weddingId,
                name = "Shri Anand Singhal & Family",
                familyName = "Singhal Pariwar",
                side = "BRIDE_SIDE",
                phone = "+91 98290 33445",
                address = "Civil Lines, Kota",
                city = "Kota",
                rsvpStatus = "ACCEPTED",
                invitationSent = true,
                numberOfMembers = 3,
                attendanceConfirmed = true,
                accommodationRequired = true,
                hotelRoomAllocated = "Room 204 (Indana)",
                foodPreference = "Jain Food"
            ),
            GuestEntity(
                weddingId = weddingId,
                name = "Dr. Mahendra Joshi",
                familyName = "Joshi Pariwar",
                side = "GROOM_SIDE",
                phone = "+91 97840 12398",
                address = "Bikaner, Rajasthan",
                city = "Bikaner",
                rsvpStatus = "TENTATIVE",
                invitationSent = true,
                numberOfMembers = 2,
                attendanceConfirmed = false,
                accommodationRequired = true,
                foodPreference = "Pure Veg"
            ),
            GuestEntity(
                weddingId = weddingId,
                name = "Shri Vijay Rathi & Family",
                familyName = "Rathi Pariwar",
                side = "BRIDE_SIDE",
                phone = "+91 98280 99112",
                address = "Udaipur",
                city = "Udaipur",
                rsvpStatus = "ACCEPTED",
                invitationSent = true,
                numberOfMembers = 5,
                attendanceConfirmed = true,
                accommodationRequired = true,
                hotelRoomAllocated = "Room 302 (Indana)",
                foodPreference = "Pure Veg"
            )
        )
    }

    fun createDemoTasks(weddingId: Long): List<TaskEntity> {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        return listOf(
            TaskEntity(
                weddingId = weddingId,
                taskName = "Finalize Sangeet Choreographer & Rehearsal Schedule",
                description = "Book 5 days family practice sessions in hall",
                assignedTo = "Amit Vyas (Brother)",
                dueDate = now + (15L * dayMillis),
                priority = "HIGH",
                status = "IN_PROGRESS",
                category = "Entertainment"
            ),
            TaskEntity(
                weddingId = weddingId,
                taskName = "Purchase 50 Barati Pachrangi Safas",
                description = "Select Jodhpuri royal bandhani safas with brooch",
                assignedTo = "Uncle Rajesh (Mama Ji)",
                dueDate = now + (25L * dayMillis),
                priority = "HIGH",
                status = "PENDING",
                category = "Shopping"
            ),
            TaskEntity(
                weddingId = weddingId,
                taskName = "Order Traditional Sweet Gift Boxes (Ghevar, Mawa Kachori)",
                description = "200 premium boxes from Jodhpur Sweets",
                assignedTo = "Shri Ramesh Vyas (Father)",
                dueDate = now + (30L * dayMillis),
                priority = "MEDIUM",
                status = "PENDING",
                category = "Shopping"
            ),
            TaskEntity(
                weddingId = weddingId,
                taskName = "Coordinate Room Allocation Chart for Outstation Guests",
                description = "Finalize check-in list with Indana Palace front desk",
                assignedTo = "Pankaj (Groom)",
                dueDate = now + (40L * dayMillis),
                priority = "HIGH",
                status = "PENDING",
                category = "Guest Logistics"
            ),
            TaskEntity(
                weddingId = weddingId,
                taskName = "Puja Samagri Collection with Pandit Ji Checklist",
                description = "Verify all 26 rituals materials are in storage room",
                assignedTo = "Smt. Sunita Vyas (Mother)",
                dueDate = now + (50L * dayMillis),
                priority = "HIGH",
                status = "PENDING",
                category = "Ritual Prep"
            )
        )
    }

    fun createDemoNotes(weddingId: Long): List<NoteEntity> {
        return listOf(
            NoteEntity(
                weddingId = weddingId,
                title = "Traditional Marwadi Menu Selection",
                content = "Starters: Pyaaz Kachori, Mirchi Vada, Paneer Tikka\nMain Course: Dal Baati Churma (5 types of churma: Rose, Besan, Mawa, Wheat, Dryfruit), Ker Sangri, Govind Gatta, Kaju Curry\nDesserts: Jodhpuri Mawa Kachori, Rabdi Ghevar, Moong Dal Halwa",
                category = "Menu & Food"
            ),
            NoteEntity(
                weddingId = weddingId,
                title = "Haldi & Mehendi Decor Color Palettes",
                content = "Haldi: Bright Sun Yellow + Marigold Orange with brass urlis and floating yellow lotus.\nMehendi: Vibrant Teal Blue + Parrot Green + Hot Pink with Bandhani canopies and Rajasthani puppets.",
                category = "Decoration Ideas"
            ),
            NoteEntity(
                weddingId = weddingId,
                title = "Pandit Ji Instructions for Saptapadi",
                content = "Keep 7 betel leaves in a line on the ground with silver coins on each leaf. Groom's sister needs to have 5 betel nuts for Gathbandhan.",
                category = "Pandit Ji Instructions"
            )
        )
    }

    fun createDemoGifts(weddingId: Long): List<GiftEntity> {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        return listOf(
            GiftEntity(
                weddingId = weddingId,
                giftItem = "Silver Lakshmi-Ganesh Idol Set (500g)",
                giverName = "Uncle Suresh Sharma",
                relationship = "Chacha Ji",
                familySide = "GROOM_SIDE",
                estimatedValue = 45000.0,
                dateReceived = now - (25L * dayMillis),
                returnGiftGiven = "Kashmiri Shawl & Dryfruit Box",
                isThankYouSent = true,
                notes = "Gifted during Sagai ceremony"
            ),
            GiftEntity(
                weddingId = weddingId,
                giftItem = "Pure Gold Chain with Om Pendant (20g)",
                giverName = "Rajesh Sharma (Mama Ji)",
                relationship = "Maternal Uncle",
                familySide = "GROOM_SIDE",
                estimatedValue = 140000.0,
                dateReceived = now - (20L * dayMillis),
                returnGiftGiven = "Silk Safa & Silver Coin",
                isThankYouSent = true,
                notes = "Given for Tilak blessing"
            )
        )
    }
}
