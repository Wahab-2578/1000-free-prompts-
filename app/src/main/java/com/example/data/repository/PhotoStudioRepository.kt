package com.example.data.repository

import com.example.data.model.FrameItem
import com.example.data.model.PhotoTemplate

object PhotoStudioRepository {
    val templates: List<PhotoTemplate> = listOf(
        // Couple
        PhotoTemplate("c1", "Romantic Cinematic Couple", "Couple", "Two people walking in soft sunset along picturesque European boulevard", "Cinematic golden hour two-person portrait walking together down Paris boulevard, soft filmic lighting, award-winning cinematography", "Cinematic", "4:5", 2),
        PhotoTemplate("c2", "Beach Sunset Couple", "Couple", "Walking along wet tidal sand under violet and golden sunset sky", "Romantic couple on coastal shoreline, gentle ocean waves reflecting twilight glow, windblown natural hair", "Photorealistic", "16:9", 2),
        PhotoTemplate("c3", "Luxury Yacht Couple", "Couple", "Elegant couple relaxing on teak deck of luxury yacht in Monaco", "Ultra-luxury lifestyle photography, tailored linen garments, Mediterranean blue water background", "Luxury", "16:9", 2),
        PhotoTemplate("c4", "Rainy Cinematic Couple", "Couple", "Couple sharing a transparent umbrella in rain-dusted city street", "Neon reflections on wet asphalt, backlit raindrops, cozy affectionate mood, 35mm lens", "Cinematic", "4:5", 2),
        PhotoTemplate("c5", "Traditional Wedding Couple", "Couple", "Grand regal wedding ceremony couple with intricate embroidered attire", "Opulent royal setting, warm velvet drapery, majestic lighting, intricate gold embroidery details", "Editorial", "4:5", 2),
        PhotoTemplate("c6", "Cozy Coffee Couple", "Couple", "Laughing together at a sunlit cafe window table with warm lattes", "Natural morning sun, soft steam, candid smiling expressions, genuine warmth", "Photorealistic", "1:1", 2),

        // Best Friends
        PhotoTemplate("f1", "Best Friends City Street", "Best Friends", "Two best friends laughing candidly crossing a sunny downtown street", "High energy candid street photography, stylish modern streetwear, sun flare, authentic joy", "Photorealistic", "4:5", 2),
        PhotoTemplate("f2", "Adventure Hike Friends", "Best Friends", "Two companions overlooking breathtaking mountain valley at sunrise", "Scenic alpine peaks, crisp morning daylight, outdoor adventure gear, epic panoramic scale", "Travel", "16:9", 2),
        PhotoTemplate("f3", "Vintage Polaroid Friends", "Best Friends", "Nostalgic retro snapshot of friends sitting on vintage convertible car", "Warm 1990s film aesthetic, analog grain, pastel tones, timeless carefree youth", "Vintage", "1:1", 2),
        PhotoTemplate("f4", "Studio Duo Fashion", "Best Friends", "High-fashion editorial duo portrait with coordinated monochrome styling", "Sculptural studio key light, sleek minimal backdrop, Vogue magazine cover aesthetic", "Editorial", "3:4", 2),

        // Brother & Sister
        PhotoTemplate("bs1", "Childhood Memory Style", "Brother & Sister", "Brother and sister sharing a heartfelt laugh in sunlit wildflower field", "Golden hour warmth, nostalgic film tones, dandelion fluff in breeze, pure familial bond", "Photorealistic", "4:5", 2),
        PhotoTemplate("bs2", "Traditional Festive Portrait", "Brother & Sister", "Sibling portrait in celebratory traditional festive attire with marigolds", "Rich cultural colors, intricate silk and velvet textures, warm celebratory ambient glow", "Editorial", "4:5", 2),
        PhotoTemplate("bs3", "Urban Lifestyle Siblings", "Brother & Sister", "Modern sibling portrait leaning against architectural textured stone wall", "Clean geometric shadows, natural overcast lighting, relaxed fashionable poses", "Studio", "1:1", 2),

        // Wedding
        PhotoTemplate("w1", "Bridal Fine Art Portrait", "Wedding", "Bride in delicate French lace veil illuminated by soft stained glass light", "Ethereal veil translucency, white calla lilies bouquet, tender emotional atmosphere", "Editorial", "4:5", 1),
        PhotoTemplate("w2", "Groom Regal Portrait", "Wedding", "Groom adjusting bespoke cufflink in stately library setting", "Warm mahogany wood tones, soft directional spotlight, sharp tailoring, aristocratic calm", "Luxury", "4:5", 1),
        PhotoTemplate("w3", "Outdoor Garden Ceremony", "Wedding", "Couple surrounded by cascading floral arch and rose petals", "Sunlight filtering through blooming wisteria, emotional celebration, fine-art wedding photography", "Photorealistic", "16:9", 2),
        PhotoTemplate("w4", "Black Tie Reception Dance", "Wedding", "Dramatic first dance in grand ballroom under sparkling crystal chandeliers", "Gleaming dance floor reflections, slow shutter light trails, romance and grandeur", "Cinematic", "16:9", 2),

        // Family
        PhotoTemplate("fam1", "Generations Family Portrait", "Family", "Multi-generational family gathered comfortably on plush living room sofa", "Warm fireplace glow, genuine smiles, soft textured wool throws, documentary warmth", "Photorealistic", "16:9", 3),
        PhotoTemplate("fam2", "Eid Celebration Family", "Family", "Family dressed in elegant Eid attire offering warm festive greetings", "Luminous festive lanterns, celebratory home decor, rich emerald and gold tones", "Editorial", "16:9", 3),
        PhotoTemplate("fam3", "Autumn Park Family Walk", "Family", "Parents and children strolling together along path lined with golden maple trees", "Falling orange leaves, crisp autumn sunlight, cozy scarves, natural candid affection", "Photorealistic", "16:9", 3),

        // Solo
        PhotoTemplate("s1", "Executive CEO Headshot", "Solo", "Modern executive with confident approachable posture in glass office", "Crisp 85mm portrait lens, blurred skyline backdrop, soft 3-point studio lighting", "Professional", "4:5", 1),
        PhotoTemplate("s2", "Cinematic Moody Portrait", "Solo", "Dramatic side-lit portrait with soft shadow falloff and catchlights", "Chiaroscuro lighting, deep rich shadows, introspective cinematic gaze", "Cinematic", "1:1", 1),
        PhotoTemplate("s3", "Luxury Penthouse Solo", "Solo", "Stylish individual gazing at twilight city lights from high-rise balcony", "Evening city lights bokeh, soft glass reflection, tailored evening wear", "Luxury", "9:16", 1),
        PhotoTemplate("s4", "Creative Studio Avatar", "Solo", "Striking clean profile on muted slate background for social media", "High clarity skin details, sharp eye reflections, modern minimalist portraiture", "Studio", "1:1", 1)
    )

    val frames: List<FrameItem> = listOf(
        FrameItem("frm1", "Minimalist Titanium Bezel", "Luxury", "Liquid Glass", "Ultra-thin metallic silver border with 12dp frosted glass corner accents"),
        FrameItem("frm2", "Golden Hour Glow Frame", "Couple", "Warm Gold", "Soft amber diffuse border gradient with floating light particles"),
        FrameItem("frm3", "Royal Emerald Velvet Frame", "Wedding", "Opulent", "Deep rich emerald border with subtle filigree corner embossing"),
        FrameItem("frm4", "Classic Film Negative 35mm", "Memories", "Vintage", "Black film strip border with sprocket holes and authentic frame numbers"),
        FrameItem("frm5", "Floating Glass Pane", "Modern", "Glassmorphic", "Multi-layered translucent glass border with prismatic chromatic edges"),
        FrameItem("frm6", "Festive Eid Crescent Frame", "Eid", "Festive", "Delicate gold crescent and star filigree with warm lantern illumination"),
        FrameItem("frm7", "Friendship Collage Grid", "Friendship", "Contemporary", "Two-panel split frame with seamless frosted divider line"),
        FrameItem("frm8", "Graduation Laurels Frame", "Graduation", "Academic", "Subtle embossed gold laurel branches along lower border"),
        FrameItem("frm9", "Artisan Travertine Border", "Luxury", "Architectural", "Natural textured stone border with crisp geometric beveled corners"),
        FrameItem("frm10", "Social Story Neon Edge", "Social Media", "Modern", "Soft ambient slate cyan border glow formatted for 9:16 vertical displays")
    )
}
