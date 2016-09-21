CREATE TABLE "Emitter" ("Id" INTEGER PRIMARY KEY  NOT NULL ,"Name" TEXT,"Lifetime" FLOAT,"EmittionRate" FLOAT,"RotationSpeed" FLOAT,"MinPos" VARCHAR,"MaxPos" VARCHAR, "ShowParticle" BOOL DEFAULT TRUE, "ParticleId" INTEGER DEFAULT 0, "ShowStreak" BOOL DEFAULT FALSE, "StreakId" INTEGER DEFAULT 0);
CREATE TABLE "Particle" ("Id" INTEGER PRIMARY KEY  NOT NULL ,"Name" TEXT,"MinDir" VARCHAR,"MaxDir" VARCHAR,"MinAxisRotSpeed" INTEGER,"MaxAxisRotSpeed" INTEGER,"MinScale" FLOAT,"MaxScale" FLOAT,"Lifetime" FLOAT,"ImageString" TEXT,"VerticalGravity" FLOAT DEFAULT (0.0) ,"HorizontalGravity" FLOAT DEFAULT (0.0) ,"StartColor" VARCHAR,"EndColor" VARCHAR,"RotationSpeed" FLOAT DEFAULT (0.0) ,"FadeSpeed" FLOAT DEFAULT (0.5) );
CREATE TABLE "ability" ("Id" INTEGER PRIMARY KEY ,"Name" TEXT,"Color" Varchar(15),"ManaCost" integer,"Cooldown" INTEGER,"AoE" VARCHAR,"Damage" INTEGER,"Range" INTEGER,"Instant" INTEGER,"Price" INTEGER,"TargetType" VARCHAR,"TargetSelf" INTEGER,"StatusEffects" VARCHAR,"DamageType" VARCHAR,"WeaponDamageFactor" FLOAT,"ClassLevel" INTEGER DEFAULT (null) ,"ProjectileId" INTEGER,"EquipReq" VARCHAR,"Description" VARCHAR,"GraphicsNr" INTEGER,"SpawnIds" VARCHAR,"Delay" INTEGER,"ClassId" INTEGER,"FamilyId" INTEGER,"AnimationId" INTEGER DEFAULT (0) ,"CastingSpeed" INTEGER DEFAULT (0) ,"ProjectileEffectId" INTEGER DEFAULT (0) , "JobSkillId" INTEGER DEFAULT 0, "BuffOrNot" INTEGER DEFAULT 0);
CREATE TABLE "ability_statuseffect" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , "Name" VARCHAR, "StatsModif" VARCHAR DEFAULT None, "Duration" INTEGER DEFAULT 0, "RepeatDamage" INTEGER DEFAULT 0, "RepeatDamageType" VARCHAR DEFAULT None, "Color" VARCHAR DEFAULT '0,0,0', "ClassId" INTEGER DEFAULT 0, "GraphicsNr" INTEGER DEFAULT 0, "AnimationId" INTEGER DEFAULT 0, "Sfx" VARCHAR DEFAULT None);
CREATE TABLE "bad_words" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , "Word" VARCHAR, "Replacement" VARCHAR);
CREATE TABLE "bounty_ranks" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , "Name" VARCHAR, "Bounty" INTEGER);
CREATE TABLE "bountyhut" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , "NpcId" INTEGER);
CREATE TABLE "card" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "Type" VARCHAR DEFAULT Creature, "Name" VARCHAR, "ItemId" INTEGER DEFAULT 0, "GraphicsId" INTEGER DEFAULT 0, "Rarity" INTEGER DEFAULT 0);
CREATE TABLE "creature" ("Id" INTEGER PRIMARY KEY  DEFAULT (NULL) ,"Name" Varchar(30) DEFAULT ('') ,"FamilyId" integer DEFAULT (0) ,"WeaponY" integer DEFAULT (0) ,"WeaponX" integer DEFAULT (0) ,"HeadY" integer DEFAULT (0) ,"HeadX" integer DEFAULT (0) ,"Ability3" integer DEFAULT (0) ,"Ability2" integer DEFAULT (0) ,"Ability1" integer DEFAULT (0) ,"SizeW" integer DEFAULT (1) ,"Boss" integer DEFAULT (0) ,"RespawnTime" integer DEFAULT (0) ,"AggroRange" integer DEFAULT (0) ,"STRENGTH" integer DEFAULT (0) ,"INTELLIGENCE" integer DEFAULT (0) ,"AGILITY" integer DEFAULT (0) ,"SPEED" integer DEFAULT (0) ,"ACCURACY" integer DEFAULT (0) ,"EVASION" integer DEFAULT (0) ,"CRITICAL_HIT" integer DEFAULT (0) ,"AttackType" Varchar(20) DEFAULT ('STRIKE') ,"MAX_HEALTH" integer DEFAULT (0) ,"MAX_MANA" integer DEFAULT (0) ,"GiveXP" integer DEFAULT (0) ,"Level" integer DEFAULT (1) ,"OffHandX" integer DEFAULT (0) ,"OffHandY" INTEGER DEFAULT (0) ,"AmuletX" INTEGER DEFAULT (0) ,"AmuletY" INTEGER DEFAULT (0) ,"ArtifactX" INTEGER DEFAULT (0) ,"ArtifactY" INTEGER DEFAULT (0) ,"FIRE_DEF" INTEGER DEFAULT (0) ,"COLD_DEF" INTEGER DEFAULT (0) ,"SHOCK_DEF" INTEGER DEFAULT (0) ,"CHEMS_DEF" INTEGER DEFAULT (0) ,"MIND_DEF" INTEGER DEFAULT (0) ,"ARMOR" INTEGER DEFAULT (0) ,"LootItems" VARCHAR,"LootCopper" INTEGER DEFAULT (0) ,"ClassId" INTEGER DEFAULT (0) ,"SizeH" INTEGER DEFAULT (1) ,"DeathAbility" INTEGER DEFAULT (0) ,"MouthFeatureX" INTEGER DEFAULT (0) ,"MouthFeatureY" INTEGER DEFAULT (0) ,"AccessoriesX" INTEGER DEFAULT (0) ,"AccessoriesY" INTEGER DEFAULT (0) ,"SkinFeatureX" INTEGER DEFAULT (0) ,"SkinFeatureY" INTEGER DEFAULT (0) ,"PlayerCreature" INTEGER DEFAULT (0) ,"MonsterWeaponIds" VARCHAR DEFAULT ('None') ,"MonsterHeadIds" VARCHAR DEFAULT ('None') ,"MonsterOffHandIds" VARCHAR DEFAULT ('None') ,"Ability4" INTEGER DEFAULT (0) ,"Ability5" INTEGER DEFAULT (0) ,"Ability6" INTEGER DEFAULT (0) , "AttackSpeed" INTEGER DEFAULT 100, "MAGIC_DEF" INTEGER DEFAULT 0);
CREATE TABLE "cute_words" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , "Word" VARCHAR);
CREATE TABLE "event" ("Id" INTEGER PRIMARY KEY  NOT NULL ,"Image" INTEGER,"Text" VARCHAR, "Sfx" VARCHAR DEFAULT None);
CREATE TABLE "item" ("Id" INT,"Name" TEXT,"ReqLevel" NUM,"Type" TEXT,"SubType" TEXT,"MinDamage" INT,"MaxDamage" INT,"ACCURACY" INT,"AGILITY" INT,"ARMOR" INT,"AttackAbility" INT,"AttackType" TEXT,"CHEMS_DEF" INT,"ClassId" INT,"COLD_DEF" INT,"ContainerSize" INT,"CRITICAL_HIT" INT,"DamageType" TEXT,"Description" TEXT,"EVASION" INT,"Family" TEXT,"FIRE_DEF" INT,"INTELLIGENCE" INT,"Material" TEXT,"MAX_HEALTH" INT,"MAX_MANA" INT,"MIND_DEF" INT,"ProjectileId" INT,"Range" INT,"ReqAgility" NUM,"ReqIntelligence" NUM,"ReqStrength" NUM,"ScrollUseId" INT,"Sellable" INT,"SHOCK_DEF" INT,"SPEED" INT,"Stackable" INT,"StatusEffects" TEXT,"STRENGTH" INT,"TwoHands" INT,"Value" NUM,"AttackSpeed" INT,"MAGIC_DEF" INTEGER DEFAULT (0) );
CREATE TABLE "item_magic" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , "Name" VARCHAR, "ExtraBonus" VARCHAR DEFAULT None, "StatusEffectId" INTEGER DEFAULT 0, "Color" VARCHAR);
CREATE TABLE "item_modifier" ("Id" INTEGER PRIMARY KEY  NOT NULL ,"Name" VARCHAR,"StatBonus" INTEGER,"ExtraBonus" VARCHAR,"DropRate" INTEGER,"MobLevel" INTEGER DEFAULT (0) , "Color" VARCHAR);
CREATE TABLE "projectile" ("Id" INTEGER PRIMARY KEY  NOT NULL ,"GfxName" VARCHAR,"EmitterId" INTEGER DEFAULT (0) , "HitColor" VARCHAR, "Sfx" VARCHAR DEFAULT None);
CREATE TABLE "recipe" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , "RecipeId" INTEGER, "Materials" VARCHAR, "ProductId" INTEGER, "CraftingStation" VARCHAR, "SkillId" INTEGER DEFAULT 0, "SkillLevel" INTEGER DEFAULT 0);
CREATE TABLE "shop" ("Id" INTEGER PRIMARY KEY  NOT NULL ,"NpcId" INTEGER,"Items" VARCHAR, "Abilities" VARCHAR DEFAULT None, "Name" VARCHAR);
CREATE TABLE "skill" ("Id" INTEGER PRIMARY KEY  NOT NULL ,"Name" VARCHAR,"Type" VARCHAR);
CREATE TABLE "treasure_map" ("Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , "X" INTEGER, "Y" INTEGER, "Z" INTEGER, "Items" VARCHAR, "Level" INTEGER DEFAULT 1, "MonsterId" INTEGER DEFAULT 0);
CREATE VIEW "amulets" AS    select Id, Name, ReqLevel, STRENGTH, AGILITY, INTELLIGENCE, SPEED, FIRE_DEF, COLD_DEF, SHOCK_DEF, CHEMS_DEF, MIND_DEF,MAGIC_DEF, ARMOR, MAX_HEALTH, MAX_MANA from item WHERE Type = 'Amulet';
CREATE VIEW "artifacts" AS select Id, Name, ReqLevel, STRENGTH, AGILITY, INTELLIGENCE, MAX_HEALTH, MAX_MANA, FIRE_DEF, SHOCK_DEF, COLD_DEF, CHEMS_DEF, MAGIC_DEF, ARMOR from item where Type = 'Artifact';
CREATE VIEW "card_items" AS  select * from item where Type = 'Collector Card';
CREATE VIEW "helmets" AS  select Id, Name, ReqLevel, ARMOR from item where Type = 'Head' order by ReqLevel asc;
CREATE VIEW "hunter_weapons" AS  SELECT Id, Name, ReqLevel, * FROM item WHERE Type = 'Weapon' AND ClassId = 3;
CREATE VIEW "item_reordered" AS SELECT Id, Name, * FROM item;
CREATE VIEW "mage_weapons" AS    SELECT Id, Name, ReqLevel, * FROM item WHERE Type = 'Weapon' AND (ClassId = 2 OR ClassId = 5 OR ClassId = 8 OR ClassId = 11) ORDER BY ReqLevel ASC;
CREATE VIEW "monster_level" AS        SELECT Id, Name, Level, MAX_HEALTH, STRENGTH, AGILITY, INTELLIGENCE, GiveXp, RespawnTime, LootCopper FROM creature WHERE PlayerCreature != 1 ORDER BY Level ASC;
CREATE VIEW "offhands" AS  select Id, Name, ReqLevel, ARMOR, ClassId FROM item WHERE Type = 'OffHand';
CREATE VIEW "warrior_weapons" AS        select Id, Name, ReqLevel, SubType, MinDamage, MaxDamage, DamageType,AttackSpeed, Value,Range from item WHERE Type = 'Weapon' AND (ClassId = 1 OR ClassId = 4 OR ClassId = 7);
CREATE VIEW "weapons" AS       select Id, Name, ReqLevel, MinDamage, MaxDamage, SubType, ClassId, DamageType, AttackType, * from item WHERE Type = 'Weapon';

drop table if exists item_gathering;
CREATE TABLE "item_gathering" (
  "ItemName" VARCHAR PRIMARY KEY NOT NULL,
  "SourceName" VARCHAR,
  "SkillLevel" INTEGER,
  "SkillId" INTEGER,
  "ResourceId" INTEGER
);
insert into item_gathering values ('gathering/oranga',      'Oranga Bush',  1,102,178);
insert into item_gathering values ('gathering/piccoberries','Picco Berries',2,102,203);
insert into item_gathering values ('gathering/matchanuts',  'Matcha Tree',  3,102,486);
insert into item_gathering values ('gathering/soulbush',    'Soul Bush',    4,102,313);

insert into item_gathering values ('gathering/skarrot',   'Skarrot Root',1,102,501);
insert into item_gathering values ('gathering/gronion',   'Gronion Root',2,102,502);
insert into item_gathering values ('gathering/topato',    'Topato Root', 3,102,503);
insert into item_gathering values ('gathering/Tripfle',   'Tripfle Root',4,102,505);

insert into item_gathering values ('gathering/flowerluna','Luna Petal',-5,102,212);
insert into item_gathering values ('gathering/Spongeroot','Spongeroot', 5,102,504);

insert into item_gathering values ('gathering/herb499','Nimt',    -2,102,499);
insert into item_gathering values ('gathering/herb498','Nasil',   -2,102,498);
insert into item_gathering values ('gathering/herb497','Ging',    -2,102,497);
insert into item_gathering values ('gathering/herb496','Noxu',    -2,102,496);
insert into item_gathering values ('gathering/herb495','Bronea',  -2,102,495);
insert into item_gathering values ('gathering/herb494','Shorogyt',-2,102,494);
insert into item_gathering values ('gathering/herb493','Hexteria',-2,102,493);
insert into item_gathering values ('gathering/herb492','Hawkloon',-2,102,492);
insert into item_gathering values ('gathering/herb491','Yammoe',  -2,102,491);
insert into item_gathering values ('gathering/herb490','Ociramma',-2,102,490);
insert into item_gathering values ('gathering/herb489','Dognip',  -2,102,489);
insert into item_gathering values ('gathering/herb488','Nalpure', -2,102,488);
insert into item_gathering values ('gathering/herb487','Kopako',  -2,102,487);

insert into item_gathering values ('gathering/herb487','Zestpond',-2,102,486);
insert into item_gathering values ('gathering/herb487','Sweet Lunabutton',-2,102,485);

insert or replace into item values (178,"Oranga",        0,"Eatable","Fruit",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"", 30, 10,0,0,0,0,0,0,0,0,0,0,100,'None',0,0, 1,0,0);
insert or replace into item values (203,"Picco Berries", 0,"Eatable","Fruit",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"", 60, 20,0,0,0,0,0,0,0,0,0,0,100,'None',0,0, 2,0,0);
insert or replace into item values (486,"Matchnuts",     0,"Eatable","Fruit",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",100, 40,0,0,0,0,0,0,0,0,0,0,100,'None',0,0, 4,0,0);
insert or replace into item values (313,"Soul Fruit",    0,"Eatable","Fruit",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",150, 80,0,0,0,0,0,0,0,0,0,0,100,'None',0,0,10,0,0);

insert or replace into item values (501,"Skarrot",       0,"Eatable","Root", 0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"", 10, 20,0,0,0,0,0,0,0,0,0,0,100,'None',0,0, 2,0,0);
insert or replace into item values (502,"Gronion",       0,"Eatable","Root", 0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"", 30, 40,0,0,0,0,0,0,0,0,0,0,100,'None',0,0, 4,0,0);
insert or replace into item values (503,"Topato",        0,"Eatable","Root", 0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"", 50, 70,0,0,0,0,0,0,0,0,0,0,100,'None',0,0, 8,0,0);
insert or replace into item values (505,"Tripfle",       0,"Eatable","Root", 0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"", 70,100,0,0,0,0,0,0,0,0,0,0,100,'None',0,0,12,0,0);

insert or replace into item values (504,"Spongeroot",    0,"Material","Root", 0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"", 0,0,0,0,0,0,0,0,0,0,0,0,100,'None',0,0,5,0,0);

--                                   Id|Name               |ReqLevel
--                                                           |Type    |SubType|MinDamage
--                                                                              |MaxDamage
--                                                                                |ACCURACY
--                                                                                  |AGILITY
--                                                                                    |ARMOR
--                                                                                      |AttackAbility
--                                                                                        |AttackType
--                                                                                               |CHEMS_DEF
--                                                                                                 |ClassId
--                                                                                                   |COLD_DEF
--                                                                                                     |ContainerSize
--                                                                                                       |CRITICAL_HIT
--                                                                                                         |DamageType
--                                                                                                                |Description
--                                                                                                                   |EVASION
--                                                                                                                     |Family
--                                                                                                                            |FIRE_DEF
--                                                                                                                              |INTELLIGENCE
--                                                                                                                                |Material
--                                                                                                                                   |MAX_HEALTH
--                                                                                                                                         |MAX_MANA
--                                                                                                                                             |MIND_DEF
--                                                                                                                                               |ProjectileId
--                                                                                                                                                 |Range
--                                                                                                                                                   |ReqAgility
--                                                                                                                                                     |ReqIntelligence
--                                                                                                                                                       |ReqStrength
--                                                                                                                                                         |ScrollUseId
--                                                                                                                                                           |Sellable
--                                                                                                                                                             |SHOCK_DEF
--                                                                                                                                                               |SPEED
--                                                                                                                                                                 |Stackable
--                                                                                                                                                                     |StatusEffects
--                                                                                                                                                                       |STRENGTH
--                                                                                                                                                                         |TwoHands
--                                                                                                                                                                           |Value
--                                                                                                                                                                              |AttackSpeed
--                                                                                                                                                                               |MAGIC_DEF
insert or replace into item values (499,"Nimt Leaf",        0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (498,"Nasil Leaf",       0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (497,"Ging Leaf",        0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (496,"Noxu Leaf",        0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (495,"Bronea Leaf",      0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (494,"Shorogyt Leaf",    0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (493,"Hexteria Leaf",    0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (492,"Hawkloon Leaf",    0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (491,"Yammoe Leaf",      0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (490,"Ociramma Leaf",    0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (489,"Dognip Leaf",      0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (488,"Nalpure Leaf",     0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
insert or replace into item values (487,"Kopako Leaf",      0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",    0,  0,0,0,0,0,0,0,0,0,0,0,100,100,0,0,0,0,0);
--                                    3|Small Health Potion|0|Potion   |HEALTH|0|0| |0|0|0|      |0|0|0|0| |None  |  | |      |0|0|None| 60|  0|0|0|0|0|0|0|0|1|0|0| 10| |0|0|10|0|0
--                                    4|Medium Health Potio|0|Potion   |HEALTH|0|0| |0|0|0|      |0|0|0|0| |None  |  | |      |0|0|None|120|  0|0|0|0|0|0|0|0|1|0|0| 10| |0|0|30|0|0
--                                    5|Small Mana Potion  |0|Potion   |MANA  |0|0| |0|0|0|      |0|0|0|0| |None  |  | |      |0|0|None|  0|100|0|0|0|0|0|0|0|1|0|0| 10| |0|0|10|0|0
--                                   6|Medium Mana Potion |0|Potion   |MANA  |0|0| |0|0|0|      |0|0|0|0| |None  |  | |      |0|0|None|  0|240|0|0|0|0|0|0|0|1|0|0| 10| |0|0|50|0|0
--                                  143|Large Health Potion|0|Potion   |HEALTH|0|0| |0|0|0|      |0|0|0|0| |None  |  | |      |0|0|None|360|  0|0|0|0|0|0|0|0|1|0|0| 10| |0|0|100|0|0

--99|STRENGTH,2;MAX_HEALTH-20|  Nimt Leaf
--98|INTELLIGENCE,2    M-10  Nasil Leaf
--97|AGILITY,2       H-20  Ging Leaf
--96|CHEMS_DEF,10    H-20  Noxu Leaf
--95|COLD_DEF,10    H-20  Bronea Leaf
--94|FIRE_DEF,10    H-20  Shorogyt Leaf
--93|MIND_DEF,10    M-10  Hexteria Leaf
--92|SHOCK_DEF,10    H-20  Hawkloon Leaf
--91|MAGIC_DEF,10    M-10  Yammoe Leaf
--90|CRITICAL,5   M-10  Ociramma Leaf
--89|EVASION,5    M-10  Dognip Leaf
--88|ACCURACY,5   M-10  Nalpure Leaf
--87|ARMOR,5      H-20  Kopako Leaf
--86|SPEED,5      H-20  Zestpond Leaf
--85|ATTACKSPEED,5    H-20  Sweet lunabutton


--CREATE TABLE "item_magic" (
--  "Id" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE,
--  "Name" VARCHAR,
--  "ExtraBonus" VARCHAR DEFAULT None,
--  "StatusEffectId" INTEGER DEFAULT 0,
--  "Color" VARCHAR
--);

--1|Burning|FIRE_DEF,2   |0|230,82,62
--2|Frozen |COLD_DEF,2   |0|0,150,255
--3|Shock  |SHOCK_DEF,2  |0|233,255,43
--4|Mighty |STRENGTH,2   |0|255,138,0
--5|Toxic  |CHEMS_DEF,2  |0|247,36,250
--6|Haste  |ATTACKSPEED,2|0|186,255,96


insert or replace into item values (486,"Zestpond Leaf",    0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",0,0,0,0,0,0,0,0,0,0,0,0,100,0,0,0,5,0,0);
insert or replace into item values (485,"Sweet lunabutton", 0,"Eatable","Herb",0,0,0,0,0,0,"None",0,0,0,0,0,"None","",0,"None",0,0,"",0,0,0,0,0,0,0,0,0,0,0,0,100,0,0,0,5,0,0);
