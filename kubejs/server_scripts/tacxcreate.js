onEvent('recipes', event => {
    
    event.recipes.createCompacting('kubejs:incomplete_556x45_item', '3x #forge:plates/brass').heated(),
    
    event.recipes.createCompacting('kubejs:incomplete_58x42_item', '3x #forge:plates/iron').heated(),
    
    event.recipes.createCompacting('kubejs:incomplete_762x39_item', '3x #forge:plates/iron').heated(),
    
    event.recipes.createCompacting('kubejs:incomplete_68x51fury_item', '3x #forge:plates/brass').heated(),
        
    event.recipes.createCompacting('kubejs:incomplete_762x54_item', ['6x #forge:plates/brass','#forge:gems/lapis']).heated(),
        
    event.recipes.createCompacting('kubejs:incomplete_9mm_item', '18x #forge:nuggets/brass').heated(),
        
    event.recipes.createCompacting('kubejs:incomplete_46x30_item', '18x #forge:nuggets/brass').heated(),
    
    event.recipes.createCompacting('kubejs:incomplete_762x25_item', '18x #forge:nuggets/brass').heated(),
        
    event.recipes.createCompacting('kubejs:incomplete_45acp_item', '18x #forge:nuggets/brass').heated(),
        
    event.recipes.createCompacting('kubejs:incomplete_50ae_item', ['54x #forge:nuggets/brass','#forge:gems/lapis']).heated(),
    
    event.recipes.createCompacting('kubejs:incomplete_30_item', '3x #forge:ingots/brass').heated(),
        
    event.recipes.createCompacting('kubejs:incomplete_308_item', ['6x #forge:ingots/brass','#forge:gems/lapis']).heated(),
    
    event.recipes.createCompacting('kubejs:incomplete_338_item', ['6x #forge:ingots/brass', '#forge:gems/lapis']).heated(),
        
    event.recipes.createCompacting('kubejs:incomplete_50bmg_item', ['20x #forge:ingots/iron', '#forge:rods/blaze','2x #forge:gems/diamond','2x #forge:ingots/gold']).heated(),
    
    event.recipes.createMixing('tac:10_gauge_round', ['18x #forge:nuggets/iron', '3x #forge:ingots/iron', '3x #forge:gunpowder']),
    
    event.recipes.createSequencedAssembly(
    [
         Item.of('60x tac:nato_556_bullet'),
    ], 
    'kubejs:incomplete_556x45_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_556x45', 'kubejs:incomplete_556x45').processingTime(20),
        event.recipes.createDeploying('kubejs:incomplete_556x45', ['kubejs:incomplete_556x45', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_556x45', ['kubejs:incomplete_556x45', '#forge:nuggets/iron'])
    ]
    ).transitionalItem('kubejs:incomplete_556x45').loops(1),
    
    event.recipes.createSequencedAssembly(
    [
         Item.of('50x tac:58x42'),
    ], 
    'kubejs:incomplete_58x42_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_58x42', 'kubejs:incomplete_58x42').processingTime(20),
        event.recipes.createDeploying('kubejs:incomplete_58x42', ['kubejs:incomplete_58x42', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_58x42', ['kubejs:incomplete_58x42', '#forge:nuggets/iron'])
    ]
        ).transitionalItem('kubejs:incomplete_58x42').loops(1),

    event.recipes.createSequencedAssembly(
    [
         Item.of('45x tac:762x39'),
    ], 
    'kubejs:incomplete_762x39_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_762x39', 'kubejs:incomplete_762x39').processingTime(20),
        event.recipes.createDeploying('kubejs:incomplete_762x39', ['kubejs:incomplete_762x39', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_762x39', ['kubejs:incomplete_762x39', '#forge:nuggets/iron'])
    ]
        ).transitionalItem('kubejs:incomplete_762x39').loops(1),
    
    event.recipes.createSequencedAssembly(
    [
         Item.of('45x tac:bullet68'),
    ], 
    'kubejs:incomplete_68x51fury_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_68x51fury', 'kubejs:incomplete_68x51fury').processingTime(20),
        event.recipes.createDeploying('kubejs:incomplete_68x51fury', ['kubejs:incomplete_68x51fury', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_68x51fury', ['kubejs:incomplete_68x51fury', '#forge:nuggets/iron'])
    ]
        ).transitionalItem('kubejs:incomplete_68x51fury').loops(1),
        
    event.recipes.createSequencedAssembly(
    [
         Item.of('50x tac:762x54'),
    ], 
    'kubejs:incomplete_762x54_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_762x54', 'kubejs:incomplete_762x54').processingTime(20),
        event.recipes.createDeploying('kubejs:incomplete_762x54', ['kubejs:incomplete_762x54', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_762x54', ['kubejs:incomplete_762x54', '#forge:nuggets/iron'])
    ]
        ).transitionalItem('kubejs:incomplete_762x54').loops(1),
    
    event.recipes.createSequencedAssembly(
    [
         Item.of('60x tac:bullet_308'),
    ], 
    'kubejs:incomplete_308_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_308', 'kubejs:incomplete_308').processingTime(20),
        event.recipes.createDeploying('kubejs:incomplete_308', ['kubejs:incomplete_308', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_308', ['kubejs:incomplete_308', '#forge:nuggets/iron'])
    ]
    ).transitionalItem('kubejs:incomplete_308').loops(1),
        
    event.recipes.createSequencedAssembly(
    [
         Item.of('60x tac:9mm_round'),
    ], 
    'kubejs:incomplete_9mm_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_9mm', 'kubejs:incomplete_9mm').processingTime(15),
        event.recipes.createDeploying('kubejs:incomplete_9mm', ['kubejs:incomplete_9mm', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_9mm', ['kubejs:incomplete_9mm', '#forge:nuggets/iron'])
    ]
    ).transitionalItem('kubejs:incomplete_9mm').loops(1),
        
    event.recipes.createSequencedAssembly(
    [
         Item.of('50x tac:46x30'),
    ], 
    'kubejs:incomplete_46x30_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_46x30', 'kubejs:incomplete_46x30').processingTime(15),
        event.recipes.createDeploying('kubejs:incomplete_46x30', ['kubejs:incomplete_46x30', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_46x30', ['kubejs:incomplete_46x30', '#forge:nuggets/iron'])
    ]
    ).transitionalItem('kubejs:incomplete_46x30').loops(1),
    
    event.recipes.createSequencedAssembly(
    [
         Item.of('45x tac:round45'),
    ], 
    'kubejs:incomplete_45acp_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_45acp', 'kubejs:incomplete_45acp').processingTime(15),
        event.recipes.createDeploying('kubejs:incomplete_45acp', ['kubejs:incomplete_45acp', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_45acp', ['kubejs:incomplete_45acp', '#forge:nuggets/iron'])
    ]
    ).transitionalItem('kubejs:incomplete_45acp').loops(1),
    
    event.recipes.createSequencedAssembly(
    [
         Item.of('50x tac:762x25'),
    ], 
    'kubejs:incomplete_762x25_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_762x25', 'kubejs:incomplete_762x25').processingTime(15),
        event.recipes.createDeploying('kubejs:incomplete_762x25', ['kubejs:incomplete_762x25', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_762x25', ['kubejs:incomplete_762x25', '#forge:nuggets/iron'])
    ]
    ).transitionalItem('kubejs:incomplete_762x25').loops(1),
    
    event.recipes.createSequencedAssembly(
    [
         Item.of('48x tac:ae50'),
    ], 
    'kubejs:incomplete_50ae_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_50ae', 'kubejs:incomplete_50ae').processingTime(50),
        event.recipes.createDeploying('kubejs:incomplete_50ae', ['kubejs:incomplete_50ae', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_50ae', ['kubejs:incomplete_50ae', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_50ae', ['kubejs:incomplete_50ae', '#forge:nuggets/gold']),
        event.recipes.createPressing('kubejs:incomplete_50ae', 'kubejs:incomplete_50ae')
    ]
    ).transitionalItem('kubejs:incomplete_50ae').loops(1),

    event.recipes.createSequencedAssembly(
    [
         Item.of('24x tac:win_30-30'),
    ], 
    'kubejs:incomplete_30_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_30', 'kubejs:incomplete_30').processingTime(30),
        event.recipes.createDeploying('kubejs:incomplete_30', ['kubejs:incomplete_30', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_30', ['kubejs:incomplete_30', '#forge:nuggets/iron'])
    ]
    ).transitionalItem('kubejs:incomplete_30').loops(1),
    
    event.recipes.createSequencedAssembly(
    [
         Item.of('24x tac:lapua338'),
    ], 
    'kubejs:incomplete_338_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_338', 'kubejs:incomplete_338').processingTime(30),
        event.recipes.createDeploying('kubejs:incomplete_338', ['kubejs:incomplete_338', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_338', ['kubejs:incomplete_338', '#forge:nuggets/iron'])
    ]
        ).transitionalItem('kubejs:incomplete_338').loops(1),
        
    event.recipes.createSequencedAssembly(
    [
         Item.of('24x tac:50bmg'),
    ], 
    'kubejs:incomplete_50bmg_item', 
        [
        event.recipes.createCutting('kubejs:incomplete_50bmg', 'kubejs:incomplete_50bmg').processingTime(50),
        event.recipes.createDeploying('kubejs:incomplete_50bmg', ['kubejs:incomplete_50bmg', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_50bmg', ['kubejs:incomplete_50bmg', '#forge:gunpowder']),
        event.recipes.createDeploying('kubejs:incomplete_50bmg', ['kubejs:incomplete_50bmg', '#forge:nuggets/gold']),
        event.recipes.createPressing('kubejs:incomplete_50bmg', 'kubejs:incomplete_50bmg')
    ]
        ).transitionalItem('kubejs:incomplete_50bmg').loops(2),
    
        event.stonecutting('kubejs:incomplete_556x45_item', 'kubejs:bullet_materials'),
        
        event.stonecutting('kubejs:incomplete_68x51fury_item','kubejs:bullet_materials'),

        event.stonecutting('kubejs:incomplete_762x39_item', 'kubejs:bullet_materials'),

        event.stonecutting('kubejs:incomplete_762x54_item', 'kubejs:bullet_materials'),

        event.stonecutting('kubejs:incomplete_58x42_item', 'kubejs:bullet_materials'),

        event.stonecutting('kubejs:incomplete_308_item', 'kubejs:bullet_materials'),

        event.stonecutting('kubejs:incomplete_338_item', 'kubejs:bullet_materials'),

        event.stonecutting('kubejs:incomplete_30_item', 'kubejs:bullet_materials'),
        
        event.stonecutting('kubejs:incomplete_9mm_item', 'kubejs:bullet_materials'),

        event.stonecutting('kubejs:incomplete_45acp_item', 'kubejs:bullet_materials'),

        event.stonecutting('kubejs:incomplete_46x30_item', 'kubejs:bullet_materials'),

        event.shaped('kubejs:bullet_materials', [
        '   ',
        'abd',
        'acd'
      ], {
        'a': '#forge:ingots/brass',
        'b': '#forge:plates/brass',
        'c': '#forge:plates/iron',
        'd': '#forge:ingots/iron'
        
    })
})
