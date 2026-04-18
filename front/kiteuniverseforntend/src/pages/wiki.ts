/**
 * Player Wiki page — static game reference content for 虚空回廊.
 */

interface WikiSection {
  id: string;
  title: string;
  icon: string;
  entries: WikiEntry[];
}

interface WikiEntry {
  term: string;
  definition: string;
}

/**
 * 虚空回廊（Void Corridor）
 * 一款以"多维裂缝"为背景的卡牌肉鸽游戏。玩家扮演「裂隙探索者」，
 * 每局随机生成的回廊中收集卡牌、拾取遗物、挑战精英与首领，
 * 最终触达回廊核心以封印次元崩塌。
 */
const WIKI_SECTIONS: WikiSection[] = [
  {
    id: 'basics',
    title: '核心机制',
    icon: '🌀',
    entries: [
      {
        term: '虚空回廊',
        definition: '游戏的核心场景结构。每局游戏随机生成一条由若干「层」组成的回廊，玩家从层 1 出发，逐层向回廊核心推进。回廊共分三幕，每幕末尾设有一名首领（Boss）。'
      },
      {
        term: '探索者（职业）',
        definition: '玩家扮演的角色类型，决定初始卡组、初始遗物与核心技能方向。当前共有四名探索者：刃影、术士、缚魂、钢躯，每位均有独立的卡牌池。'
      },
      {
        term: '卡牌',
        definition: '战斗中的核心资源。每回合从手牌中选择并打出卡牌来攻击、防御或触发效果。卡牌分为攻击、技能、能力三大类，稀有度分为普通、罕见、特殊三级。'
      },
      {
        term: '能量',
        definition: '每回合行动的核心消耗资源。多数卡牌需要消耗 1–3 点能量才能打出。每回合开始时自动恢复至上限（默认 3 点），部分遗物或卡牌可永久提升上限。'
      },
      {
        term: '遗物',
        definition: '在层间节点拾取的被动道具，提供持续增益效果。遗物有普通、罕见、传说三个品质，传说遗物通常附带特殊的副作用或限制条件。'
      },
      {
        term: '祝福',
        definition: '击败精英或首领后可选择的增益选项，效果仅限本局有效。祝福效果通常强于遗物，但部分祝福会触发相应的「诅咒」作为平衡代价。'
      },
      {
        term: '诅咒',
        definition: '加入手牌的负面卡牌，打出时不产生任何效果，还会占用能量。部分机制可将诅咒转化为战斗资源（如缚魂职业的「炼咒」技能）。'
      },
      {
        term: '层节点',
        definition: '每层随机分布若干节点，包括：战斗、精英战斗、商店、营地（休息/锻造）、事件和首领。节点类型由图标标注，可提前规划路线。'
      },
      {
        term: '营地',
        definition: '可以选择「休息」（恢复 30% 最大生命值）或「锻造」（升级一张手牌）。进入首领层前通常会强制路过一个营地。'
      },
      {
        term: '金币',
        definition: '通关战斗或拾取事件奖励获得，用于商店消费。商店可购买卡牌、遗物、药水，也可以付费移除手牌中的卡牌（精简卡组的核心手段）。'
      }
    ]
  },
  {
    id: 'classes',
    title: '探索者职业',
    icon: '🧙',
    entries: [
      {
        term: '刃影',
        definition: '近战连击型职业。核心资源是「连击层数」，每打出一张攻击牌叠加一层，部分技能在连击层数达到阈值后触发爆发效果。擅长积累动能后的爆发输出，上手曲线较平缓。'
      },
      {
        term: '术士',
        definition: '法术爆发型职业。手牌中的「法球」在每回合结束时自动触发效果，卡牌打出后会留下新的法球。擅长多法球叠加的连锁爆发，但生命值较低，容错率不高。'
      },
      {
        term: '缚魂',
        definition: '召唤牺牲型职业。通过「召唤灵体」在场上建立从者，并以消耗灵体为代价触发强力效果。核心玩法是管理灵体数量与牺牲节奏，操作复杂但上限极高。'
      },
      {
        term: '钢躯',
        definition: '防御反伤型职业。以积累「护甲」为核心，护甲不会在回合结束时清除（不同于格挡），可以在敌人攻击时以超量护甲转化为伤害反弹。适合喜欢稳健节奏的玩家。'
      }
    ]
  },
  {
    id: 'combat',
    title: '战斗机制',
    icon: '⚔️',
    entries: [
      {
        term: '格挡',
        definition: '回合内的临时防御值，受到攻击时优先消耗格挡而非生命值。格挡在每回合结束时清零（钢躯的护甲除外）。通过「防御」类卡牌或部分遗物获得。'
      },
      {
        term: '虚弱',
        definition: '负面状态。处于虚弱状态的单位造成的攻击伤害降低 25%。持续若干回合后自动解除，可通过药水或特定卡牌提前清除。'
      },
      {
        term: '易伤',
        definition: '负面状态。处于易伤状态的单位受到的攻击伤害提高 50%。是最高价值的减益之一，通常由术士的法球或特定遗物附加。'
      },
      {
        term: '中毒',
        definition: '持续伤害状态。每回合开始时对目标造成等同于中毒层数的伤害，伤害结算后层数 -1。中毒可叠加，多目标中毒流是缚魂的核心输出路线之一。'
      },
      {
        term: '灼烧',
        definition: '持续伤害状态。每回合结束时造成等同于灼烧层数的伤害，但层数不会自然衰减，需要特定手段清除。灼烧总伤害通常高于中毒，但需要更多回合发酵。'
      },
      {
        term: '力量',
        definition: '永久增益状态。每点力量使本单位每次攻击额外造成 1 点伤害。与攻击次数而非攻击卡牌数量挂钩，因此连击型刃影与多段攻击卡从力量中受益最大。'
      },
      {
        term: '意图',
        definition: '敌人行动的预告系统。每个敌人头顶会显示当前回合的行动图标，包括攻击（数值）、防御（盾牌）、增益（箭头）等，玩家可据此规划出牌顺序。'
      },
      {
        term: '精英战斗',
        definition: '比普通战斗更强力的遭遇，敌人通常携带独有技能和更高面板，但击败后必定掉落一件遗物，是跑图中获取遗物的重要途径。'
      }
    ]
  },
  {
    id: 'bosses',
    title: '首领与精英',
    icon: '👁️',
    entries: [
      {
        term: '三幕首领',
        definition: '每幕末尾的强力Boss。第一幕首领：裂隙先驱；第二幕首领：镜中执政、虚空伶官（二选一随机）；第三幕首领：次元崩塌者（终局）。每位首领均有独特的战斗规则。'
      },
      {
        term: '裂隙先驱',
        definition: '第一幕Boss。形态较为简单，交替使用蓄力攻击与护盾技能。核心机制是「裂痕标记」：当标记叠满 3 层后会触发全体伤害。适合新玩家学习意图阅读。'
      },
      {
        term: '镜中执政',
        definition: '第二幕Boss之一。能复制玩家上回合打出的最后一张牌并对自身使用，若玩家打出强力自我增益牌则会被「镜像」回来，需要格外注意打牌节奏。'
      },
      {
        term: '虚空伶官',
        definition: '第二幕Boss之一。以「幕帷」机制为核心：每隔一回合进入幕帷状态，该状态下免疫所有伤害但同时无法行动。玩家需要在幕帷关闭的回合内集中爆发输出。'
      },
      {
        term: '次元崩塌者',
        definition: '最终Boss。分三个阶段，每阶段削减至特定血量后触发形态切换。第三阶段会将玩家的全部手牌洗回牌库并随机加入诅咒。是对卡组整体性的终极检验。'
      },
      {
        term: '精英：虚影傀儡',
        definition: '精英敌人之一。能在场上召唤若干虚影分身，分身与本体共享伤害并可独立攻击。优先消灭分身可大幅降低受击压力，但本体会在分身全灭后进入狂暴状态。'
      },
      {
        term: '精英：深渊凝视者',
        definition: '精英敌人之一。核心技能「凝视」每次使用后会给玩家加入一张诅咒「眼球」。若手牌中存在 3 张及以上眼球诅咒，凝视者会触发即死效果，需优先处理诅咒。'
      }
    ]
  },
  {
    id: 'cards',
    title: '卡牌机制',
    icon: '🃏',
    entries: [
      {
        term: '升级',
        definition: '在营地「锻造」或通过遗物/事件升级一张牌。升级后的卡牌名称后缀「+」，通常提升伤害值、降低费用或附加额外效果。升级不改变稀有度或卡牌类型。'
      },
      {
        term: '消耗',
        definition: '带有「消耗」关键字的卡牌打出后会从牌库中永久移除，不参与后续循环。消耗牌通常具有超强的即时效果，是高爆发路线的核心组成部分。'
      },
      {
        term: '临时',
        definition: '带有「临时」关键字的卡牌在回合结束时自动丢弃，不进入弃牌堆也不进入牌库循环。通常由特定遗物或祝福生成，效果强力但不持久。'
      },
      {
        term: '以太',
        definition: '带有「以太」关键字的卡牌在回合结束时会自动打出而非丢弃，不消耗能量。适合用于零费/负费连锁构建，也可以绕过特定的手牌上限限制。'
      },
      {
        term: '牌库循环',
        definition: '当抽牌堆耗尽时，弃牌堆会自动洗入抽牌堆重新使用，称为一次「循环」。卡组越精简，循环速度越快，核心牌的出手频率越高。精简卡组（移除弱牌）是进阶要点。'
      },
      {
        term: '零费牌',
        definition: '费用为 0 的卡牌，打出时不消耗能量。零费牌在卡组中的比例影响「流量」：零费牌过多时每回合有效行动数量提升，但过少则拖慢节奏。'
      },
      {
        term: '药水',
        definition: '战斗或事件中获得的即用道具，可在任意时机使用，通常提供紧急治疗、格挡或状态效果。携带上限为 3 瓶，若满格则新药水无法拾取。'
      }
    ]
  },
  {
    id: 'meta',
    title: '进阶与挑战',
    icon: '🏆',
    entries: [
      {
        term: '天命等级（Ascension）',
        definition: '俗称「A几」，通关一次后解锁下一级天命，每级在原有难度上叠加新的惩罚机制（如敌人强化、首领附加技能、商店价格提升等）。最高天命为 A20。'
      },
      {
        term: '天命修正',
        definition: '高天命等级下额外叠加的机制变量。A10 起，精英敌人会携带随机「强化词条」；A15 起，玩家每层开始时会随机失去一张手牌；A20 起，每幕Boss均进入强化形态。'
      },
      {
        term: '日常挑战',
        definition: '每日刷新的特殊跑图规则，通常包含特定的起始卡组、遗物或地图限制。完成日常挑战可获得专属称号和解锁点数，所有玩家共享同一条件以便比较成绩。'
      },
      {
        term: '无限流',
        definition: '玩家术语，指通过特定卡牌与遗物组合实现"无限出牌"循环的构建。最常见的无限循环依赖「以太」类卡牌 + 能量生成遗物。无限流一旦建立通常可秒杀一切。'
      },
      {
        term: '心脏路线',
        definition: '俗称「打心」，指在 A20 难度下通关并对抗隐藏最终Boss「次元之心」所需的特殊路线。需要在各幕的精英战斗中收集三个钥匙（虚空钥匙/血肉钥匙/灵魂钥匙）。'
      },
      {
        term: '解锁点数',
        definition: '完成特定成就、挑战、天命晋升后获得的点数，用于解锁各职业的新卡牌和初始遗物变体，增加跑图的构建多样性。'
      }
    ]
  }
];

export const WikiPage = {
  data() {
    return {
      sections: WIKI_SECTIONS,
      activeSection: 'basics',
      searchQuery: ''
    };
  },

  computed: {
    currentSection(this: any): WikiSection | undefined {
      return this.sections.find((s: WikiSection) => s.id === this.activeSection);
    },
    filteredEntries(this: any): WikiEntry[] {
      const section = this.currentSection;
      if (!section) return [];
      const q = this.searchQuery.trim().toLowerCase();
      if (!q) return section.entries;
      return section.entries.filter((e: WikiEntry) =>
        e.term.toLowerCase().includes(q) || e.definition.toLowerCase().includes(q)
      );
    },
    allFilteredEntries(this: any): (WikiEntry & { sectionTitle: string })[] {
      if (!this.searchQuery.trim()) return [];
      const q = this.searchQuery.trim().toLowerCase();
      const results: (WikiEntry & { sectionTitle: string })[] = [];
      for (const section of this.sections) {
        for (const entry of section.entries) {
          if (entry.term.toLowerCase().includes(q) || entry.definition.toLowerCase().includes(q)) {
            results.push({ ...entry, sectionTitle: section.title });
          }
        }
      }
      return results;
    }
  },

  template: `
    <div class="wiki-page">
      <div class="page-hero">
        <div class="page-hero__eyebrow">玩家百科</div>
        <h1 class="page-hero__title">虚空回廊 Wiki</h1>
        <p class="page-hero__desc">机制解析、职业指南、首领情报与进阶技巧</p>
      </div>

      <!-- 搜索框 -->
      <div class="wiki-search">
        <input
          class="wiki-search__input"
          type="text"
          v-model="searchQuery"
          placeholder="搜索词条，如「诅咒」「钢躯」「天命」…"
        />
      </div>

      <!-- 全局搜索结果 -->
      <div v-if="searchQuery.trim()" class="wiki-search-results">
        <div v-if="allFilteredEntries.length === 0" class="empty-state">
          <div class="empty-state__text">没有找到「{{ searchQuery }}」相关词条</div>
        </div>
        <div v-else class="wiki-entry-list">
          <div v-for="entry in allFilteredEntries" :key="entry.term + entry.sectionTitle" class="wiki-entry">
            <div class="wiki-entry__header">
              <span class="wiki-entry__term">{{ entry.term }}</span>
              <span class="wiki-entry__section-badge">{{ entry.sectionTitle }}</span>
            </div>
            <div class="wiki-entry__definition">{{ entry.definition }}</div>
          </div>
        </div>
      </div>

      <!-- 分区导航 + 内容 -->
      <div v-else class="wiki-layout">
        <nav class="wiki-nav">
          <button
            v-for="section in sections"
            :key="section.id"
            class="wiki-nav__item"
            :class="{ 'wiki-nav__item--active': activeSection === section.id }"
            @click="activeSection = section.id"
          >
            <span class="wiki-nav__icon">{{ section.icon }}</span>
            {{ section.title }}
          </button>
        </nav>

        <div class="wiki-content">
          <div v-if="currentSection" class="wiki-entry-list">
            <div v-for="entry in filteredEntries" :key="entry.term" class="wiki-entry">
              <div class="wiki-entry__term">{{ entry.term }}</div>
              <div class="wiki-entry__definition">{{ entry.definition }}</div>
            </div>
            <div v-if="filteredEntries.length === 0" class="empty-state">
              <div class="empty-state__text">该分区暂无词条</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
};
