import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const resources = path.join(root, 'common/src/main/resources');
const generated = path.join(root, 'common/src/generated');
const write = (base, name, data) => {
  const target = path.join(base, name);
  fs.mkdirSync(path.dirname(target), {recursive:true});
  fs.writeFileSync(target, JSON.stringify(data, null, 2) + '\n');
};
const read = (base, name) => JSON.parse(fs.readFileSync(path.join(base,name),'utf8'));
const profiles = read(root, 'compat/profiles.json');
write(resources, 'wildslabs-compat.json', profiles.map(({source,kind})=>({source,kind})));
const slabIds = ['wildslabs:farmland_slab'];
const soilIds = [];
const tillableIds = [];
const lang = {};

function model(textures, y0, y1, grassy = false, tinted = false) {
  const faces = {};
  for (const direction of ['down','up','north','south','west','east']) {
    const horizontal = !['down','up'].includes(direction);
    faces[direction] = {
      uv: horizontal ? [0,0,16,y1-y0] : [0,0,16,16],
      texture: direction === 'down' ? '#bottom' : direction === 'up' || grassy ? '#top' : '#side'
    };
    if (tinted && (direction === 'up' || horizontal && grassy)) faces[direction].tintindex = 0;
    if (horizontal || direction === 'down' && y0 === 0 || direction === 'up' && y1 === 16) faces[direction].cullface = direction;
  }
  const elements=[{from:[0,y0,0],to:[16,y1,16],faces}];
  if (textures.overlay && !grassy) {
    const overlayFaces=Object.fromEntries(['north','south','west','east'].map(direction=>[direction,{uv:[0,0,16,y1-y0],texture:'#overlay',tintindex:0,cullface:direction}]));
    elements.push({from:[0,y0,0],to:[16,y1,16],faces:overlayFaces});
  }
  return {parent:'minecraft:block/block', ...(textures.overlay?{render_type:'minecraft:cutout_mipped'}:{}), textures:{...textures,particle:textures.side}, elements};
}

for (const profile of profiles) {
  const [mod, block] = profile.source.split(':');
  const id = `${mod}/${block}_slab`;
  const fullId = `wildslabs:${id}`;
  slabIds.push(fullId);
  if (['soil','grass'].includes(profile.kind)) soilIds.push(fullId);
  if (profile.kind === 'grass') tillableIds.push(fullId);
  const tex = profile.textures;
  for (const [type,y0,y1] of [['bottom',0,8],['top',8,16],['double',0,16]]) {
    write(resources, `assets/wildslabs/models/block/${id}_${type}.json`, model(tex,y0,y1,false,profile.tinted));
    if (profile.kind === 'grass' || profile.betterGrass) write(resources, `resourcepacks/better_grass_slabs/assets/wildslabs/models/block/${id}_${type}.json`,model(tex,y0,y1,true,profile.tinted));
  }
  write(resources,`assets/wildslabs/blockstates/${id}.json`,{variants:Object.fromEntries(['bottom','top','double'].map(type=>[`type=${type}`,{model:`wildslabs:block/${id}_${type}`}]))});
  write(resources,`assets/wildslabs/models/item/${id}.json`,{parent:`wildslabs:block/${id}_bottom`});
  const condition = [{'type':'neoforge:mod_loaded',modid:mod}];
  const doubleCount = {function:'minecraft:set_count',count:2,conditions:[{condition:'minecraft:block_state_property',block:fullId,properties:{type:'double'}}]};
  write(generated,`data/wildslabs/loot_table/blocks/${id}.json`,{
    'neoforge:conditions':condition, type:'minecraft:block',pools:[{rolls:1,conditions:[{condition:'minecraft:survives_explosion'}],entries:[{
      type:'minecraft:alternatives',children:[
        {type:'minecraft:item',name:profile.source,conditions:[{condition:'minecraft:block_state_property',block:fullId,properties:{generated:'true'}}]},
        {type:'minecraft:item',name:fullId,functions:[doubleCount]}
      ]
    }]}]
  });
  write(generated,`data/wildslabs/recipe/${id}.json`,{
    'neoforge:conditions':condition,type:'minecraft:crafting_shaped',category:'building',pattern:['###'],key:{'#':{item:profile.source}},result:{id:fullId,count:6}
  });
  lang[`block.wildslabs.${id.replaceAll('/','.')}`] = profile.name;
}

for (const [name,values] of [
  ['minecraft/slabs',slabIds],['wildslabs/dirt_slabs',soilIds],['wildslabs/tillable_slabs',tillableIds],
  ['minecraft/mineable/shovel', profiles.filter(p=>['sand','soil','grass'].includes(p.kind)).map(p=>`wildslabs:${p.source.replace(':','/')}_slab`).concat('wildslabs:farmland_slab')],
  ['minecraft/mineable/pickaxe',profiles.filter(p=>p.kind==='stone').map(p=>`wildslabs:${p.source.replace(':','/')}_slab`)]
]) {
  const slash = name.indexOf('/');
  const file = `data/${name.slice(0,slash)}/tags/block/${name.slice(slash+1)}.json`;
  const existing = fs.existsSync(path.join(generated,file)) ? read(generated,file).values : [];
  const unique = new Map(existing.map(v=>[typeof v==='string'?v:v.id,v]));
  for (const id of values) unique.set(id,id.includes('wildslabs:biomesoplenty/') ? {id,required:false} : id);
  write(generated,file,{replace:false,values:[...unique.values()]});
}

for (const wet of [false,true]) {
  const textures = {bottom:'minecraft:block/dirt',side:'minecraft:block/dirt',top:`minecraft:block/farmland${wet?'_moist':''}`};
  for (const [type,y0,y1] of [['bottom',0,7],['top',8,15],['double',0,15]]) write(resources,`assets/wildslabs/models/block/farmland_slab_${type}_${wet?'wet':'dry'}.json`, model(textures,y0,y1));
}
write(resources,'assets/wildslabs/blockstates/farmland_slab.json',{variants:Object.fromEntries(['bottom','top','double'].flatMap(type=>Array.from({length:8},(_,moisture)=>[`type=${type},moisture=${moisture}`,{model:`wildslabs:block/farmland_slab_${type}_${moisture>0?'wet':'dry'}`}])))});
write(resources,'assets/wildslabs/models/item/farmland_slab.json',{parent:'wildslabs:block/farmland_slab_bottom_dry'});
const dirtLoot=read(generated,'data/wildslabs/loot_table/blocks/dirt_slab.json');
write(generated,'data/wildslabs/loot_table/blocks/farmland_slab.json',JSON.parse(JSON.stringify(dirtLoot).replaceAll('"block":"wildslabs:dirt_slab"','"block":"wildslabs:farmland_slab"')));

for (const localeFile of fs.readdirSync(path.join(resources,'assets/wildslabs/lang'))) {
  if (!localeFile.endsWith('.json')) continue;
  const file=`assets/wildslabs/lang/${localeFile}`;
  const data=read(resources,file);
  data['itemGroup.wildslabs']='WildSlabs';
  data['wildslabs.midnightconfig.title']='WildSlabs';
  write(resources,file,data);
}

for (const locale of ['en_us','fr_fr']) {
  const file=`assets/wildslabs/lang/${locale}.json`;
  const data=read(resources,file);
  for (const key of Object.keys(data)) if (key.startsWith('block.wildslabs.') && key.includes('/')) delete data[key];
  data['itemGroup.wildslabs']='WildSlabs';
  data['block.wildslabs.farmland_slab']=locale==='fr_fr'?'Dalle de terre labourée':'Farmland Slab';
  data['wildslabs.midnightconfig.enableBetterGrassCompatibility']=locale==='fr_fr'?'Better Grass : côtés herbeux (redémarrage)':'Better Grass: grassy sides (restart)';
  data['wildslabs.midnightconfig.terrainSeedOffset']=locale==='fr_fr'?'Décalage de graine du terrain':'Terrain seed offset';
  data['wildslabs.midnightconfig.slabPlacementChance']=locale==='fr_fr'?'Probabilité de génération des dalles (0–1)':'Slab placement probability (0–1)';
  data['wildslabs.midnightconfig.automaticModVegetationCompatibility']=locale==='fr_fr'?'Compatibilité automatique des plantes de mods':'Automatic mod vegetation compatibility';
  data['wildslabs.midnightconfig.preserveWaterways']=locale==='fr_fr'?'Préserver rivières, berges et plantes aquatiques':'Preserve rivers, shorelines and aquatic plants';
  write(resources,file,{...data,...lang});
}
const mixins=read(resources,'wildslabs.mixins.json');
for (const name of ['ontop.place.MixinCropSoil','shovel.MixinHoeItem','ontop.place.MixinPlantSurvival','ontop.place.MixinSoilProbeLevel','ontop.place.BushBlockAccessor']) if (!mixins.mixins.includes(name)) mixins.mixins.push(name);
write(resources,'wildslabs.mixins.json',mixins);
console.log(`Generated ${profiles.length} optional terrain slabs and farmland resources.`);
