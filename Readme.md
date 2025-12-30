# Adventure Seasons Mod For Fabric

Um mod de estações para Minecraft que adiciona um sistema realista de estações do ano, afetando temperatura, crescimento de plantações, cores da vegetação e muito mais.

## 📋 Características

### 🌱 Sistema de Estações
- **4 Estações Principais**: Primavera, Verão, Outono, Inverno
- **12 Subestações**: Cada estação possui Início, Meio e Fim
- Duração configurável para cada subestação

### 🌡️ Efeitos de Temperatura
- Biomas têm temperaturas modificadas por estação
- Neve no inverno em biomas configurados
- Derretimento de neve e gelo no verão

### 🌾 Crescimento de Plantações
- Crescimento mais rápido na Primavera (especialmente MID_SPRING - 2x!)
- Crescimento mais lento no Inverno (até 0.1x no MID_WINTER)
- Biomas excluídos mantêm crescimento normal

### 🎨 Mudanças Visuais

As cores de grama e folhagem mudam gradualmente através das 12 subestações, criando transições suaves e realistas.

#### 🌸 Primavera (Spring)
| Subestação | Folhagem | Grama | Descrição |
|------------|----------|-------|-----------|
| **Early Spring** | Verde-limão claro | Verde brilhante | Brotos novos emergindo |
| **Mid Spring** | Verde vibrante | Verde viçoso | Vegetação em pleno despertar |
| **Late Spring** | Verde intenso | Verde rico | Folhas totalmente desenvolvidas |

#### ☀️ Verão (Summer)  
| Subestação | Folhagem | Grama | Descrição |
|------------|----------|-------|-----------|
| **Early Summer** | Verde escuro | Verde denso | Vegetação exuberante |
| **Mid Summer** | Verde profundo | Verde escuro | Auge do crescimento |
| **Late Summer** | Verde maduro | Verde-oliva | Início da maturação |

#### 🍂 Outono (Autumn)
| Subestação | Folhagem | Grama | Descrição |
|------------|----------|-------|-----------|
| **Early Autumn** | Amarelo-esverdeado | Dourado suave | Início da mudança de cores |
| **Mid Autumn** | Laranja/Dourado | Amarelo-dourado | Cores outonais intensas |
| **Late Autumn** | Laranja-avermelhado | Marrom-dourado | Folhas prontas para cair |

#### ❄️ Inverno (Winter)
| Subestação | Folhagem | Grama | Descrição |
|------------|----------|-------|-----------|
| **Early Winter** | Marrom acinzentado | Marrom pálido | Vegetação entrando em dormência |
| **Mid Winter** | Marrom escuro | Cinza-amarronzado | Dormência total |
| **Late Winter** | Marrom com tons verdes | Marrom claro | Preparando para despertar |

#### 🌲 Tipos Especiais de Vegetação

- **Bétula (Birch)**: Cores mais claras e amareladas, especialmente no outono
- **Spruce (Abeto)**: Mantém tons verdes o ano todo, mas mais escuros no inverno
- **Pântano (Swamp)**: Cores mais escuras e úmidas, menos variação sazonal
- **Neve**: Cobre o terreno em biomas configurados durante o inverno

### 🔧 Blocos Especiais
- **Season Sensor**: Emite sinal de redstone baseado na estação atual
- **Season Calendar**: Mostra informações sobre a estação atual

## 📦 Instalação

1. Instale o [Fabric Loader](https://fabricmc.net/use/installer/)
2. Instale a [Fabric API](https://modrinth.com/mod/fabric-api)
3. Coloque o arquivo `.jar` do mod na pasta `mods`

### Requisitos
- Minecraft 1.21.1
- Fabric Loader 0.18.3+
- Fabric API 0.116.7+

## ⚙️ Configuração

O arquivo de configuração é criado em `config/adventure_seasons.json`:

```json
{
  "season_start": "SPRING",
  "winter_rain": true,
  "debug": false,
  "doTemperatureChange": true,
  "isFallAndSpringReversed": true,
  "shouldSnowyBiomesMeltInSummer": true,
  "shouldIceNearWaterMelt": false,
  "shouldSnowReplaceVegetation": true,
  "excludedBiomes": ["minecraft:desert", "minecraft:jungle"],
  "biomeForceSnowInWinterList": ["minecraft:plains"],
  "seasonLength": {
    "spring": { "earlyLength": 120000, "midLength": 480000, "lateLength": 168000 },
    "summer": { "earlyLength": 240000, "midLength": 384000, "lateLength": 288000 },
    "autumn": { "earlyLength": 192000, "midLength": 360000, "lateLength": 264000 },
    "winter": { "earlyLength": 192000, "midLength": 456000, "lateLength": 120000 }
  }
}
```

## 🎮 Comandos

| Comando | Descrição | Permissão |
|---------|-----------|-----------|
| `/season` | Mostra a estação atual | Todos |
| `/season set <subseason>` | Define a subestação | OP (nível 2) |
| `/season setseason <season>` | Define a estação | OP (nível 2) |
| `/season next` | Avança para próxima subestação | OP (nível 2) |

### Exemplos
```
/season set MID_SPRING
/season setseason WINTER
/season next
```

## 📊 Modificadores de Crescimento

| Subestação | Modificador | Descrição |
|------------|-------------|-----------|
| MID_SPRING | 2.0x | ✨ Melhor época! |
| EARLY_SPRING | 1.3x | Bom |
| LATE_SPRING | 1.5x | Muito bom |
| EARLY_SUMMER | 1.2x | Bom |
| MID_SUMMER | 0.9x | Quente demais |
| LATE_SUMMER | 1.0x | Normal |
| EARLY_AUTUMN | 0.8x | Desacelerando |
| MID_AUTUMN | 0.6x | Lento |
| LATE_AUTUMN | 0.5x | Muito lento |
| EARLY_WINTER | 0.3x | Muito frio |
| MID_WINTER | 0.1x | ❄️ Quase parado |
| LATE_WINTER | 0.4x | Melhorando |

> Nota: Se `isFallAndSpringReversed` estiver ativo, Primavera e Outono trocam seus valores.

## 🔗 Compatibilidade

- ✅ Fabric API
- ✅ Mod Menu (para visualização)
- ✅ Adventure Mod (integração automática)

## 📜 Licença

All Rights Reserved - Veja [LICENSE](LICENSE) para detalhes.

## 🤝 Contribuição

Contribuições são bem-vindas! Por favor, abra uma issue ou pull request.

## 📝 Changelog

### v1.0.0
- Lançamento inicial
- Sistema de 4 estações com 12 subestações
- Modificadores de crescimento de plantações
- Mudanças visuais de cores
- Comandos de administração
- Sistema de configuração JSON
