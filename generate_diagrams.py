import subprocess
import os

# Create directories if not exist
os.makedirs("public/diagrams", exist_ok=True)
artifact_dir = ".aistudio/artifacts/brain/47933bfc-dcc3-4736-87d9-52ccbd9ac000"
os.makedirs(artifact_dir, exist_ok=True)

# 1. System Architecture Diagram (SVG)
svg_arch = """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 820" width="1200" height="820">
  <defs>
    <style>
      .bg { fill: #ffffff; }
      .box { fill: #ffffff; stroke: #000000; stroke-width: 2; }
      .box-dashed { fill: #ffffff; stroke: #000000; stroke-width: 1.5; stroke-dasharray: 5,5; }
      .layer-box { fill: #ffffff; stroke: #000000; stroke-width: 2.5; }
      .title { font-family: 'Arial', sans-serif; font-size: 20px; font-weight: bold; fill: #000000; }
      .layer-title { font-family: 'Arial', sans-serif; font-size: 15px; font-weight: bold; fill: #000000; letter-spacing: 1px; }
      .comp-title { font-family: 'Arial', sans-serif; font-size: 14px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .comp-sub { font-family: 'Arial', sans-serif; font-size: 11px; fill: #333333; text-anchor: middle; }
      .arrow { stroke: #000000; stroke-width: 2; fill: none; marker-end: url(#arrowhead); }
      .arrow-bi { stroke: #000000; stroke-width: 1.5; fill: none; marker-end: url(#arrowhead); marker-start: url(#arrowtail); }
      .note { font-family: 'Arial', sans-serif; font-size: 11px; font-style: italic; fill: #444444; }
    </style>
    <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#000000" />
    </marker>
    <marker id="arrowtail" markerWidth="8" markerHeight="6" refX="1" refY="3" orient="auto">
      <polygon points="8 0, 0 3, 8 6" fill="#000000" />
    </marker>
  </defs>

  <rect width="100%" height="100%" class="bg" />

  <!-- Main Title -->
  <text x="600" y="40" class="title" text-anchor="middle">NUTRIPATH: SYSTEM ARCHITECTURE</text>
  <line x1="380" y1="48" x2="820" y2="48" stroke="#000000" stroke-width="1.5" />

  <!-- 1. PRESENTATION LAYER -->
  <rect x="50" y="70" width="1100" height="180" class="layer-box" rx="6" />
  <text x="70" y="95" class="layer-title">1. PRESENTATION LAYER (ANDROID JETPACK COMPOSE UI)</text>

  <!-- Presentation Components -->
  <rect x="80" y="115" width="220" height="110" class="box" rx="4" />
  <text x="190" y="145" class="comp-title">Onboarding &amp; Profile</text>
  <text x="190" y="165" class="comp-sub">• Anthropometrics &amp; Sex</text>
  <text x="190" y="180" class="comp-sub">• Chronic Conditions (T2D, CKD)</text>
  <text x="190" y="195" class="comp-sub">• Active Medication Entry</text>

  <rect x="330" y="115" width="260" height="110" class="box" rx="4" />
  <text x="460" y="145" class="comp-title">Meal Generator &amp; Craving UI</text>
  <text x="460" y="165" class="comp-sub">• Time-Aware Selection</text>
  <text x="460" y="180" class="comp-sub">• Engine Switcher (AI vs Catalog)</text>
  <text x="460" y="195" class="comp-sub">• Natural Language Craving Input</text>

  <rect x="620" y="115" width="250" height="110" class="box" rx="4" />
  <text x="745" y="145" class="comp-title">Recipe Cards &amp; Rationale</text>
  <text x="745" y="165" class="comp-sub">• AI Clinical Adherence Rationale</text>
  <text x="745" y="180" class="comp-sub">• Behavioral Adherence Tips</text>
  <text x="745" y="195" class="comp-sub">• Interactive Ingredient Exclusions</text>

  <rect x="900" y="115" width="220" height="110" class="box" rx="4" />
  <text x="1010" y="145" class="comp-title">Portion &amp; Daily Tracker</text>
  <text x="1010" y="165" class="comp-sub">• AdjustPortionDialog (Preset/Slider)</text>
  <text x="1010" y="180" class="comp-sub">• Real-time Macro Recalculation</text>
  <text x="1010" y="195" class="comp-sub">• Consumed Nutrient Rings</text>

  <!-- Connectors: Presentation to Domain -->
  <line x1="190" y1="225" x2="190" y2="280" class="arrow" />
  <line x1="460" y1="225" x2="460" y2="280" class="arrow" />
  <line x1="745" y1="225" x2="745" y2="280" class="arrow" />
  <line x1="1010" y1="225" x2="1010" y2="280" class="arrow" />

  <!-- 2. APPLICATION & DOMAIN LAYER -->
  <rect x="50" y="280" width="1100" height="290" class="layer-box" rx="6" />
  <text x="70" y="305" class="layer-title">2. APPLICATION &amp; DOMAIN LOGIC LAYER (MVVM ARCHITECTURE)</text>

  <!-- ViewModel -->
  <rect x="80" y="325" width="240" height="225" class="box" rx="4" />
  <text x="200" y="355" class="comp-title">NutriPathViewModel</text>
  <line x1="90" y1="365" x2="310" y2="365" stroke="#000000" stroke-width="1" />
  <text x="200" y="385" class="comp-sub">• StateFlow Reactive Streams</text>
  <text x="200" y="405" class="comp-sub">• Coroutine Dispatchers (IO/Main)</text>
  <text x="200" y="425" class="comp-sub">• Daily Budget Accumulation</text>
  <text x="200" y="445" class="comp-sub">• Dynamic Portion State Handler</text>
  <text x="200" y="465" class="comp-sub">• Fallback Notice Coordinator</text>
  <text x="200" y="485" class="comp-sub">• ADIME Note Dispatcher</text>

  <!-- Gemini Repository -->
  <rect x="350" y="325" width="240" height="225" class="box" rx="4" />
  <text x="470" y="355" class="comp-title">GeminiMealRepository</text>
  <line x1="360" y1="365" x2="580" y2="365" stroke="#000000" stroke-width="1" />
  <text x="470" y="385" class="comp-sub">• Clinical Profile Prompt Synthesis</text>
  <text x="470" y="405" class="comp-sub">• Structured JSON Parser (Moshi)</text>
  <text x="470" y="425" class="comp-sub">• Adherence Rationale Formatter</text>
  <text x="470" y="445" class="comp-sub">• Zero-Downtime Fallback Manager</text>
  <text x="470" y="465" class="comp-sub">• Asynchronous REST Dispatch</text>

  <!-- Safety & Clinical Validator -->
  <rect x="620" y="325" width="240" height="225" class="box" rx="4" />
  <text x="740" y="355" class="comp-title">MealSafetyValidator (Zero-Trust)</text>
  <line x1="630" y1="365" x2="850" y2="365" stroke="#000000" stroke-width="1" />
  <text x="740" y="385" class="comp-sub">• Drug-Nutrient Catalog (FDA)</text>
  <text x="740" y="405" class="comp-sub">• Warfarin Vit-K Modulation</text>
  <text x="740" y="425" class="comp-sub">• Statin Grapefruit Exclusion</text>
  <text x="740" y="445" class="comp-sub">• Renal Potassium/Sodium Caps</text>
  <text x="740" y="465" class="comp-sub">• Status: Approved / Warning / Reject</text>

  <!-- Clinical Engines -->
  <rect x="890" y="325" width="240" height="225" class="box" rx="4" />
  <text x="1010" y="355" class="comp-title">Clinical Engines &amp; MNT</text>
  <line x1="900" y1="365" x2="1120" y2="365" stroke="#000000" stroke-width="1" />
  <text x="1010" y="385" class="comp-sub">• MealRecommendationEngine</text>
  <text x="1010" y="405" class="comp-sub">• NutritionCalculator (IFCT 2017)</text>
  <text x="1010" y="425" class="comp-sub">• ADIME Clinical Record Builder</text>
  <text x="1010" y="445" class="comp-sub">• Deterministic Fallback Catalog</text>
  <text x="1010" y="465" class="comp-sub">• HealthGenie Ingredient Adaptation</text>

  <!-- Connectors inside Domain -->
  <line x1="320" y1="437" x2="350" y2="437" class="arrow-bi" />
  <line x1="590" y1="437" x2="620" y2="437" class="arrow" />
  <line x1="860" y1="437" x2="890" y2="437" class="arrow-bi" />

  <!-- Connectors: Domain to Data -->
  <line x1="200" y1="550" x2="200" y2="600" class="arrow" />
  <line x1="470" y1="550" x2="470" y2="600" class="arrow" />
  <line x1="1010" y1="550" x2="1010" y2="600" class="arrow" />

  <!-- 3. DATA & EXTERNAL SERVICES LAYER -->
  <rect x="50" y="600" width="1100" height="190" class="layer-box" rx="6" />
  <text x="70" y="625" class="layer-title">3. DATA &amp; EXTERNAL SERVICES LAYER</text>

  <!-- Local Room DB -->
  <rect x="100" y="645" width="460" height="125" class="box" rx="4" />
  <text x="330" y="675" class="comp-title">Local Persistence (Room SQLite DB)</text>
  <line x1="110" y1="685" x2="550" y2="685" stroke="#000000" stroke-width="1" />
  <text x="330" y="705" class="comp-sub">• HealthProfileEntity (Biomarkers, Conditions, Active Medications)</text>
  <text x="330" y="725" class="comp-sub">• MealPlanEntity (Daily Schedule, Consumed Tracking, Portions)</text>
  <text x="330" y="745" class="comp-sub">• IndianFoodEntity &amp; Reference Micronutrients (IFCT &amp; Anuvaad)</text>

  <!-- Gemini REST API -->
  <rect x="640" y="645" width="460" height="125" class="box" rx="4" />
  <text x="870" y="675" class="comp-title">Google Gemini 3.5 Flash REST API</text>
  <line x1="650" y1="685" x2="1090" y2="685" stroke="#000000" stroke-width="1" />
  <text x="870" y="705" class="comp-sub">• Cloud Generative AI Service (v1beta/models/gemini-3.5-flash)</text>
  <text x="870" y="725" class="comp-sub">• Dynamic MNT Recipe Synthesis with Structured JSON Output</text>
  <text x="870" y="745" class="comp-sub">• Patient Clinical Adherence Rationale &amp; Behavioral Guidance</text>
</svg>
"""

# 2. Use Case Diagram (SVG)
svg_usecase = """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 850" width="1200" height="850">
  <defs>
    <style>
      .bg { fill: #ffffff; }
      .system-box { fill: #ffffff; stroke: #000000; stroke-width: 2.5; }
      .system-title { font-family: 'Arial', sans-serif; font-size: 18px; font-weight: bold; fill: #000000; letter-spacing: 1px; }
      .main-title { font-family: 'Arial', sans-serif; font-size: 22px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .usecase { fill: #ffffff; stroke: #000000; stroke-width: 1.8; }
      .uc-text { font-family: 'Arial', sans-serif; font-size: 13px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .uc-sub { font-family: 'Arial', sans-serif; font-size: 10.5px; fill: #444444; text-anchor: middle; }
      .actor-text { font-family: 'Arial', sans-serif; font-size: 14px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .line { stroke: #000000; stroke-width: 1.5; fill: none; }
      .line-dashed { stroke: #000000; stroke-width: 1.5; stroke-dasharray: 5,4; fill: none; marker-end: url(#arrowhead); }
      .include-label { font-family: 'Arial', sans-serif; font-size: 11px; font-style: italic; fill: #000000; text-anchor: middle; }
    </style>
    <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#000000" />
    </marker>
  </defs>

  <rect width="100%" height="100%" class="bg" />

  <!-- Main Title -->
  <text x="600" y="45" class="main-title">NUTRIPATH: USE CASE DIAGRAM</text>
  <line x1="420" y1="55" x2="780" y2="55" stroke="#000000" stroke-width="1.5" />

  <!-- System Boundary -->
  <rect x="260" y="80" width="680" height="730" class="system-box" rx="8" />
  <text x="600" y="110" class="system-title" text-anchor="middle">SYSTEM BOUNDARY: NUTRIPATH MOBILE PLATFORM</text>

  <!-- ACTOR 1: Patient / User -->
  <!-- Stick figure -->
  <circle cx="120" cy="380" r="22" stroke="#000000" stroke-width="2.5" fill="#ffffff" />
  <line x1="120" y1="402" x2="120" y2="470" stroke="#000000" stroke-width="2.5" />
  <line x1="75" y1="425" x2="165" y2="425" stroke="#000000" stroke-width="2.5" />
  <line x1="120" y1="470" x2="85" y2="540" stroke="#000000" stroke-width="2.5" />
  <line x1="120" y1="470" x2="155" y2="540" stroke="#000000" stroke-width="2.5" />
  <text x="120" y="570" class="actor-text">Patient / User</text>
  <text x="120" y="590" class="uc-sub">(Chronic Disease Patient)</text>

  <!-- ACTOR 2: Google Gemini Cloud AI -->
  <rect x="1000" y="370" width="150" height="90" stroke="#000000" stroke-width="2" fill="#ffffff" rx="6" />
  <text x="1075" y="405" class="actor-text">&lt;&lt;System&gt;&gt;</text>
  <text x="1075" y="425" class="actor-text">Google Gemini</text>
  <text x="1075" y="445" class="uc-sub">3.5 Flash REST API</text>

  <!-- USE CASES (Ellipses) -->
  <!-- UC 1 -->
  <ellipse cx="600" cy="155" rx="200" ry="32" class="usecase" />
  <text x="600" y="152" class="uc-text">UC1: Configure Health Profile</text>
  <text x="600" y="168" class="uc-sub">(Biomarkers, T2D/CKD, Medications, Macros)</text>

  <!-- UC 2 -->
  <ellipse cx="600" cy="245" rx="200" ry="32" class="usecase" />
  <text x="600" y="242" class="uc-text">UC2: Generate Personalized Meals</text>
  <text x="600" y="258" class="uc-sub">(Gemini LLM Mode / Clinical Catalog Mode)</text>

  <!-- UC 3 -->
  <ellipse cx="600" cy="335" rx="200" ry="32" class="usecase" />
  <text x="600" y="332" class="uc-text">UC3: Review Clinical Adherence Rationale</text>
  <text x="600" y="348" class="uc-sub">(Metabolic Explanations &amp; Behavioral Tips)</text>

  <!-- UC 4 -->
  <ellipse cx="600" cy="425" rx="200" ry="32" class="usecase" />
  <text x="600" y="422" class="uc-text">UC4: Interactively Exclude Ingredients</text>
  <text x="600" y="438" class="uc-sub">(Dynamic Recipe Adaptation &amp; Substitution)</text>

  <!-- UC 5 -->
  <ellipse cx="600" cy="515" rx="200" ry="32" class="usecase" />
  <text x="600" y="512" class="uc-text">UC5: Adjust Portion Size with Live Scaling</text>
  <text x="600" y="528" class="uc-sub">(Preset Chips &amp; Slider: Calories, Protein, Sodium)</text>

  <!-- UC 6 -->
  <ellipse cx="600" cy="605" rx="200" ry="32" class="usecase" />
  <text x="600" y="602" class="uc-text">UC6: Log Meal as Consumed</text>
  <text x="600" y="618" class="uc-sub">(Updates Daily Nutritional Budget &amp; History)</text>

  <!-- UC 7 -->
  <ellipse cx="600" cy="695" rx="200" ry="32" class="usecase" />
  <text x="600" y="692" class="uc-text">UC7: Inspect ADIME Clinical Chart Note</text>
  <text x="600" y="708" class="uc-sub">(Assessment, Diagnosis, Intervention, Monitoring)</text>

  <!-- UC 8 (Secondary Included) -->
  <ellipse cx="600" cy="775" rx="190" ry="26" class="usecase" />
  <text x="600" y="774" class="uc-text">UC8: Screen Drug-Nutrient Safety (DNI)</text>
  <text x="600" y="788" class="uc-sub">(Deterministic Zero-Trust Verification)</text>

  <!-- Lines from Patient to Use Cases -->
  <line x1="160" y1="420" x2="400" y2="155" class="line" />
  <line x1="165" y1="425" x2="400" y2="245" class="line" />
  <line x1="170" y1="435" x2="400" y2="335" class="line" />
  <line x1="170" y1="445" x2="400" y2="425" class="line" />
  <line x1="165" y1="455" x2="400" y2="515" class="line" />
  <line x1="160" y1="465" x2="400" y2="605" class="line" />
  <line x1="150" y1="475" x2="400" y2="695" class="line" />

  <!-- Include relationship UC2 -> UC8 -->
  <line x1="600" y1="277" x2="600" y2="749" class="line-dashed" />
  <rect x="545" y="375" width="110" height="20" fill="#ffffff" />
  <text x="600" y="389" class="include-label">&lt;&lt;include&gt;&gt;</text>

  <!-- Line from Gemini API to UC2 and UC3 -->
  <line x1="1000" y1="400" x2="800" y2="245" class="line" />
  <line x1="1000" y1="420" x2="800" y2="335" class="line" />
</svg>
"""

# 3. DFD Level 0 (Context Diagram - DeMarco Rule)
svg_dfd0 = """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 750" width="1200" height="750">
  <defs>
    <style>
      .bg { fill: #ffffff; }
      .entity-box { fill: #ffffff; stroke: #000000; stroke-width: 2.5; }
      .process-circle { fill: #ffffff; stroke: #000000; stroke-width: 2.5; }
      .store-line { stroke: #000000; stroke-width: 2.5; }
      .main-title { font-family: 'Arial', sans-serif; font-size: 22px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .proc-num { font-family: 'Arial', sans-serif; font-size: 16px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .proc-title { font-family: 'Arial', sans-serif; font-size: 16px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .entity-title { font-family: 'Arial', sans-serif; font-size: 15px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .entity-sub { font-family: 'Arial', sans-serif; font-size: 11px; fill: #444444; text-anchor: middle; }
      .flow-label { font-family: 'Arial', sans-serif; font-size: 11px; fill: #000000; font-weight: 600; }
      .arrow { stroke: #000000; stroke-width: 1.8; fill: none; marker-end: url(#arrowhead); }
    </style>
    <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#000000" />
    </marker>
  </defs>

  <rect width="100%" height="100%" class="bg" />

  <!-- Main Title -->
  <text x="600" y="45" class="main-title">DATA FLOW DIAGRAM (DFD) LEVEL 0: CONTEXT DIAGRAM</text>
  <text x="600" y="70" font-family="Arial" font-size="12" font-style="italic" fill="#555" text-anchor="middle">(DeMarco &amp; Yourdon Notation Standard)</text>
  <line x1="340" y1="80" x2="860" y2="80" stroke="#000000" stroke-width="1.5" />

  <!-- CENTRAL PROCESS: 0.0 NutriPath System (Circle) -->
  <circle cx="600" cy="380" r="140" class="process-circle" />
  <line x1="465" y1="320" x2="735" y2="320" stroke="#000000" stroke-width="1.5" />
  <text x="600" y="305" class="proc-num">0.0</text>
  <text x="600" y="360" class="proc-title">NUTRIPATH</text>
  <text x="600" y="385" class="proc-title">PERSONALIZED MNT</text>
  <text x="600" y="410" class="proc-title">&amp; DIETARY ADHERENCE</text>
  <text x="600" y="435" class="proc-title">SYSTEM</text>

  <!-- ENTITY 1: Patient / User (Rectangle) -->
  <rect x="50" y="310" width="180" height="140" class="entity-box" />
  <text x="140" y="360" class="entity-title">PATIENT / USER</text>
  <text x="140" y="385" class="entity-sub">(Chronic Disease Patient)</text>
  <text x="140" y="415" class="entity-sub">[External Entity]</text>

  <!-- Flows: Patient -> System -->
  <!-- Top Flow: Input -->
  <path d="M 230 340 L 460 340" class="arrow" />
  <text x="245" y="330" class="flow-label">1. Biometrics, Conditions &amp; Active Meds</text>

  <!-- Middle-Top Flow: Request -->
  <path d="M 230 375 L 460 375" class="arrow" />
  <text x="245" y="367" class="flow-label">2. Meal Request, Cravings &amp; Exclusions</text>

  <!-- Middle-Bottom Flow: Consumed/Portion -->
  <path d="M 230 410 L 460 410" class="arrow" />
  <text x="245" y="403" class="flow-label">3. Portions &amp; Consumed Meal Logs</text>

  <!-- Flows: System -> Patient -->
  <!-- Bottom Flow: Output -->
  <path d="M 465 440 L 230 440" class="arrow" />
  <text x="245" y="460" class="flow-label">4. Verified Recipes &amp; Safety Audit Badges</text>

  <path d="M 495 470 L 230 470" class="arrow" />
  <text x="245" y="490" class="flow-label">5. Clinical Adherence Rationales &amp; ADIME Notes</text>

  <!-- ENTITY 2: Google Gemini AI Cloud Service (Rectangle) -->
  <rect x="970" y="310" width="180" height="140" class="entity-box" />
  <text x="1060" y="355" class="entity-title">GOOGLE GEMINI</text>
  <text x="1060" y="375" class="entity-title">3.5 FLASH API</text>
  <text x="1060" y="405" class="entity-sub">(Generative AI Service)</text>
  <text x="1060" y="425" class="entity-sub">[External Entity]</text>

  <!-- Flows: System -> Gemini -->
  <path d="M 740 345 L 970 345" class="arrow" />
  <text x="750" y="335" class="flow-label">Clinical Context &amp; Macro Bounds</text>

  <!-- Flows: Gemini -> System -->
  <path d="M 970 415 L 740 415" class="arrow" />
  <text x="750" y="435" class="flow-label">Generated Recipes &amp; Adherence Rationale</text>

  <!-- DATA STORE: DeMarco Open-ended parallel lines -->
  <line x1="450" y1="640" x2="750" y2="640" class="store-line" />
  <line x1="450" y1="700" x2="750" y2="700" class="store-line" />
  <text x="470" y="675" class="proc-num">D1</text>
  <text x="600" y="675" class="entity-title">Local SQLite Data Store (Room)</text>

  <!-- Flows: System <-> Data Store -->
  <path d="M 570 520 L 570 640" class="arrow" />
  <text x="440" y="580" class="flow-label" text-anchor="end">Store Profile, Meals &amp; Logs</text>

  <path d="M 630 640 L 630 520" class="arrow" />
  <text x="645" y="580" class="flow-label">Fetch Nutrients &amp; DNI Catalog</text>
</svg>
"""

# 4. DFD Level 1 (DeMarco Notation Standard)
svg_dfd1 = """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1250 920" width="1250" height="920">
  <defs>
    <style>
      .bg { fill: #ffffff; }
      .entity-box { fill: #ffffff; stroke: #000000; stroke-width: 2.5; }
      .process-circle { fill: #ffffff; stroke: #000000; stroke-width: 2; }
      .store-line { stroke: #000000; stroke-width: 2.2; }
      .main-title { font-family: 'Arial', sans-serif; font-size: 22px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .proc-num { font-family: 'Arial', sans-serif; font-size: 13px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .proc-title { font-family: 'Arial', sans-serif; font-size: 12px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .entity-title { font-family: 'Arial', sans-serif; font-size: 14px; font-weight: bold; fill: #000000; text-anchor: middle; }
      .store-title { font-family: 'Arial', sans-serif; font-size: 12px; font-weight: bold; fill: #000000; }
      .flow-label { font-family: 'Arial', sans-serif; font-size: 10px; fill: #000000; font-weight: 600; }
      .arrow { stroke: #000000; stroke-width: 1.5; fill: none; marker-end: url(#arrowhead); }
    </style>
    <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#000000" />
    </marker>
  </defs>

  <rect width="100%" height="100%" class="bg" />

  <!-- Main Title -->
  <text x="625" y="40" class="main-title">DATA FLOW DIAGRAM (DFD) LEVEL 1: DETAILED SYSTEM PROCESSES</text>
  <text x="625" y="62" font-family="Arial" font-size="12" font-style="italic" fill="#555" text-anchor="middle">(DeMarco &amp; Yourdon Functional Decomposition)</text>
  <line x1="320" y1="72" x2="930" y2="72" stroke="#000000" stroke-width="1.5" />

  <!-- EXTERNAL ENTITY 1: Patient / User -->
  <rect x="40" y="380" width="150" height="120" class="entity-box" />
  <text x="115" y="435" class="entity-title">PATIENT / USER</text>
  <text x="115" y="455" font-family="Arial" font-size="11" fill="#444" text-anchor="middle">[External Entity]</text>

  <!-- PROCESS 1.0: Manage Health Profile -->
  <circle cx="360" cy="180" r="65" class="process-circle" />
  <line x1="300" y1="150" x2="420" y2="150" stroke="#000000" stroke-width="1.2" />
  <text x="360" y="142" class="proc-num">1.0</text>
  <text x="360" y="175" class="proc-title">Manage Health</text>
  <text x="360" y="195" class="proc-title">Profile &amp; Limits</text>

  <!-- DATA STORE D1: Health Profile Store -->
  <line x1="560" y1="155" x2="740" y2="155" class="store-line" />
  <line x1="560" y1="205" x2="740" y2="205" class="store-line" />
  <text x="575" y="185" class="store-title">D1</text>
  <text x="605" y="185" class="store-title">Health Profile Store</text>

  <!-- Flows for 1.0 -->
  <path d="M 120 380 L 120 180 L 295 180" class="arrow" />
  <text x="130" y="170" class="flow-label">Biometrics, Diseases &amp; Meds</text>

  <path d="M 425 180 L 560 180" class="arrow" />
  <text x="440" y="172" class="flow-label">Save Profile</text>

  <!-- PROCESS 2.0: Synthesize Clinical Prompts -->
  <circle cx="360" cy="380" r="65" class="process-circle" />
  <line x1="300" y1="350" x2="420" y2="350" stroke="#000000" stroke-width="1.2" />
  <text x="360" y="342" class="proc-num">2.0</text>
  <text x="360" y="375" class="proc-title">Synthesize</text>
  <text x="360" y="395" class="proc-title">Clinical Context</text>

  <!-- Flow Patient -> 2.0 -->
  <path d="M 190 410 L 295 410" class="arrow" />
  <text x="200" y="402" class="flow-label">Meal Type &amp; Cravings</text>

  <!-- Flow D1 -> 2.0 -->
  <path d="M 620 205 L 620 350 L 425 350" class="arrow" />
  <text x="480" y="340" class="flow-label">Read Profile &amp; Targets</text>

  <!-- PROCESS 3.0: Generate AI Recipes & Rationale -->
  <circle cx="680" cy="380" r="65" class="process-circle" />
  <line x1="620" y1="350" x2="740" y2="350" stroke="#000000" stroke-width="1.2" />
  <text x="680" y="342" class="proc-num">3.0</text>
  <text x="680" y="375" class="proc-title">Generate AI</text>
  <text x="680" y="395" class="proc-title">Recipes &amp; Rationale</text>

  <!-- Flow 2.0 -> 3.0 -->
  <path d="M 425 380 L 615 380" class="arrow" />
  <text x="450" y="372" class="flow-label">Structured Clinical Prompt</text>

  <!-- EXTERNAL ENTITY 2: Gemini API -->
  <rect x="1050" y="325" width="160" height="110" class="entity-box" />
  <text x="1130" y="375" class="entity-title">GEMINI 3.5 FLASH</text>
  <text x="1130" y="395" font-family="Arial" font-size="11" fill="#444" text-anchor="middle">[External AI API]</text>

  <!-- Flows 3.0 <-> Gemini -->
  <path d="M 745 365 L 1050 365" class="arrow" />
  <text x="780" y="357" class="flow-label">POST generateContent</text>

  <path d="M 1050 395 L 745 395" class="arrow" />
  <text x="780" y="412" class="flow-label">JSON Candidates &amp; Rationale</text>

  <!-- PROCESS 4.0: Validate Clinical Safety & DNI -->
  <circle cx="680" cy="580" r="65" class="process-circle" />
  <line x1="620" y1="550" x2="740" y2="550" stroke="#000000" stroke-width="1.2" />
  <text x="680" y="542" class="proc-num">4.0</text>
  <text x="680" y="575" class="proc-title">Screen Clinical</text>
  <text x="680" y="595" class="proc-title">Safety &amp; DNI</text>

  <!-- Flow 3.0 -> 4.0 -->
  <path d="M 680 445 L 680 515" class="arrow" />
  <text x="685" y="480" class="flow-label">Raw Candidate Recipes</text>

  <!-- DATA STORE D3: Drug-Nutrient Catalog -->
  <line x1="900" y1="555" x2="1080" y2="555" class="store-line" />
  <line x1="900" y1="605" x2="1080" y2="605" class="store-line" />
  <text x="915" y="585" class="store-title">D3</text>
  <text x="945" y="585" class="store-title">DNI &amp; Clinical Catalog</text>

  <!-- Flow D3 -> 4.0 -->
  <path d="M 900 580 L 745 580" class="arrow" />
  <text x="765" y="572" class="flow-label">DNI Rules &amp; Caps</text>

  <!-- Flow 4.0 -> Patient (Verified Meals) -->
  <path d="M 615 580 L 190 480" class="arrow" />
  <text x="230" y="525" class="flow-label">Verified Recipes &amp; Adherence Rationale</text>

  <!-- PROCESS 5.0: Scale Portions & Log Consumption -->
  <circle cx="360" cy="720" r="65" class="process-circle" />
  <line x1="300" y1="690" x2="420" y2="690" stroke="#000000" stroke-width="1.2" />
  <text x="360" y="682" class="proc-num">5.0</text>
  <text x="360" y="715" class="proc-title">Scale Portions &amp;</text>
  <text x="360" y="735" class="proc-title">Log Consumed</text>

  <!-- Flow Patient -> 5.0 -->
  <path d="M 100 500 L 100 720 L 295 720" class="arrow" />
  <text x="110" y="680" class="flow-label">Adjust Servings &amp; Confirm Log</text>

  <!-- Flow 4.0 -> 5.0 -->
  <path d="M 630 620 L 420 700" class="arrow" />
  <text x="500" y="650" class="flow-label">Approved Recipe</text>

  <!-- DATA STORE D2: Meal Plans & Daily Budget Store -->
  <line x1="560" y1="790" x2="740" y2="790" class="store-line" />
  <line x1="560" y1="840" x2="740" y2="840" class="store-line" />
  <text x="575" y="820" class="store-title">D2</text>
  <text x="605" y="820" class="store-title">Meal Plans &amp; Logs</text>

  <!-- Flow 5.0 -> D2 -->
  <path d="M 425 735 L 560 805" class="arrow" />
  <text x="440" y="780" class="flow-label">Persist Meal Log</text>

  <!-- PROCESS 6.0: Compile ADIME Clinical Records -->
  <circle cx="950" cy="740" r="65" class="process-circle" />
  <line x1="890" y1="710" x2="1010" y2="710" stroke="#000000" stroke-width="1.2" />
  <text x="950" y="702" class="proc-num">6.0</text>
  <text x="950" y="735" class="proc-title">Compile ADIME</text>
  <text x="950" y="755" class="proc-title">Clinical Record</text>

  <!-- Flow 4.0 -> 6.0 -->
  <path d="M 725 625 L 895 715" class="arrow" />
  <text x="730" y="685" class="flow-label">Assessment &amp; Audit Data</text>

  <!-- Flow 6.0 -> Patient -->
  <path d="M 950 805 L 950 880 L 70 880 L 70 500" class="arrow" />
  <text x="320" y="870" class="flow-label">Standardized ADIME Chart Note (NCP)</text>
</svg>
"""

# Save SVGs
with open("public/diagrams/systemarchitecture.svg", "w") as f:
    f.write(svg_arch)
with open("public/diagrams/usecasediagram.svg", "w") as f:
    f.write(svg_usecase)
with open("public/diagrams/level0.svg", "w") as f:
    f.write(svg_dfd0)
with open("public/diagrams/level1.svg", "w") as f:
    f.write(svg_dfd1)

# Convert all SVGs to high-resolution JPGs and PNGs using ImageMagick
diagram_files = ["systemarchitecture", "usecasediagram", "level0", "level1"]
for d in diagram_files:
    svg_p = f"public/diagrams/{d}.svg"
    # Convert to public/
    jpg_p1 = f"public/{d}.jpg"
    png_p1 = f"public/{d}.png"
    # Convert to root
    jpg_root = f"{d}.jpg"
    png_root = f"{d}.png"
    # Convert to artifacts
    jpg_art = f"{artifact_dir}/{d}.jpg"
    png_art = f"{artifact_dir}/{d}.png"
    
    cmd1 = f"convert -density 200 -background white -flatten {svg_p} {jpg_p1}"
    cmd2 = f"convert -density 200 -background white -flatten {svg_p} {png_p1}"
    cmd3 = f"cp {jpg_p1} {jpg_root} && cp {png_p1} {png_root}"
    cmd4 = f"cp {jpg_p1} {jpg_art} && cp {png_p1} {png_art}"
    
    subprocess.run(cmd1, shell=True, check=True)
    subprocess.run(cmd2, shell=True, check=True)
    subprocess.run(cmd3, shell=True, check=True)
    subprocess.run(cmd4, shell=True, check=True)

print("ALL 4 DIAGRAMS SUCCESSFULLY GENERATED AND CONVERTED TO JPG/PNG!")
