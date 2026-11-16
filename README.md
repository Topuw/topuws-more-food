# 🍯 Topuw's More Food (Topuw's Food Mod)

A Minecraft Fabric mod designed to revitalize and expand the culinary experience in Minecraft! Minecraft's food system has been neglected for too long — this mod introduces practical jars, cauldron cooking, sweet berry preserves, bakery items, and fried eggs.

---

## 🍲 Features

### 🫙 The Jar System
* **Jar (Słoik):** Crafted from glass blocks. Stacks up to 16! Used as a reusable container for fluids, preserves, and food.
  * Can scoop water from water sources or cauldrons.
  * Can gather honey directly from full Beehives and Bee Nests (level 5 honey).
* **Water Jar (Słoik z wodą):** Drinkable water container that leaves behind an empty jar. Can be poured into or scooped from cauldrons, and can even be caught while fishing!
* **Honey Jar (Słoik z miodem):** Restores 6 hunger points and cures poison, playing the classic honey drinking sound and returning an empty jar.
* **Sweet Berry Jam Jar (Słoik z dżemem jagodowym):** Delicious homemade jam! Always edible, restoring 6 hunger points and high saturation.

---

### 🥣 Cauldron Cooking & Preserves
* **Jam Cauldron (Kocioł z dżemem):**
  * Right-click a water cauldron with **Sweet Berries** to brew jam!
  * Add more sweet berries to increase the jam level up to 3.
  * Right-click the cauldron with an empty **Jar** to scoop up a jar of jam.
  * Refill or empty cauldrons using water jars and jam jars.
  * *Note: Jam is a thick culinary preserve found exclusively in cauldrons and jars — no messy bucket spills!*

---

### 🍞 Bakery & Cooking
* **Bread with Sweet Berry Jam (Chleb z dżemem jagodowym):**
  * Right-click a jam cauldron with a piece of **Bread**, or craft bread with a jar of jam.
  * High-tier food restoring **8 hunger points** (4 drumsticks) and great saturation.
* **Fried Egg (Smażone jajko):**
  * Smelt any egg in a standard furnace, smoker, or campfire:
    * Regular Egg (`minecraft:egg`)
    * Blue Egg (`minecraft:blue_egg` - Cold Chicken variant)
    * Brown Egg (`minecraft:brown_egg` - Warm Chicken variant)
  * Restores **5 hunger points** with solid saturation.

---

## 🤝 Community & Contributions

**Everyone is welcome to join and contribute!** 

Whether you want to:
* Propose new recipes, preserves, or crops
* Submit pixel art textures
* Fix bugs or add new culinary mechanics
* Translate the mod into other languages

Feel free to open an **Issue** or submit a **Pull Request**. Let's make Minecraft's food system rich and fun together!

---

## 🛠️ Building & Developing

This mod is built using the Fabric Loom toolchain for modern Minecraft versions.

```bash
# Clone the repository
git clone https://github.com/Topuw/topuws-more-food.git

# Build the mod JAR
./gradlew build
```

The compiled mod JAR will be located in `build/libs/`.

---

## 📄 License

This project is licensed under the [CC0 1.0 Universal License](LICENSE).
