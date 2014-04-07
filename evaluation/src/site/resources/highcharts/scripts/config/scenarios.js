define(['util', 'mark'], function (util, mark) {

    var scenario_names = [
        'scenario_1', 'scenario_2', 'scenario_3', 'scenario_4',
        'scenario_5', 'scenario_6', 'scenario_7', 'scenario_8',
        'scenario_9', 'scenario_10', 'scenario_11', 'scenario_12',
        'scenario_13', 'scenario_14', 'scenario_15', 'scenario_16',
        'scenario_17',
        'scenario_18', 'scenario_19', 'scenario_20', 'scenario_21',
        'scenario_22', 'scenario_23', 'scenario_24', 'scenario_25',
        'scenario_26', 'scenario_27', 'scenario_28', 'scenario_29',
        'scenario_30', 'scenario_31', 'scenario_32', 'scenario_33',
        'scenario_34', 'scenario_35', 'scenario_36', 'scenario_37',
        'scenario_38', 'scenario_39', 'scenario_40', 'scenario_41',
        'scenario_42', 'scenario_43', 'scenario_44', 'scenario_45',
        'scenario_46', 'scenario_47', 'scenario_48', 'scenario_49',
        'scenario_50', 'scenario_51', 'scenario_52', 'scenario_53',
        'scenario_54', 'scenario_55', 'scenario_56', 'scenario_57',
        'scenario_58', 'scenario_59', 'scenario_60', 'scenario_61',
        'scenario_62', 'scenario_63', 'scenario_64', 'scenario_65',
        'scenario_66', 'scenario_67', 'scenario_68', 'scenario_69',
        'scenario_70', 'scenario_71', 'scenario_72', 'scenario_73',
        'scenario_74', 'scenario_75', 'scenario_76', 'scenario_77',
        'scenario_78', 'scenario_79', 'scenario_80', 'scenario_81',
        'scenario_82', 'scenario_83', 'scenario_84', 'scenario_85',
        'scenario_86', 'scenario_87', 'scenario_88', 'scenario_89',
        'scenario_90', 'scenario_91', 'scenario_92', 'scenario_93',
        'scenario_94', 'scenario_95', 'scenario_96', 'scenario_97',
        'scenario_98', 'scenario_99', 'scenario_100', 'scenario_101',
        'scenario_102', 'scenario_103', 'scenario_104', 'scenario_105',
        'scenario_106', 'scenario_107', 'scenario_108', 'scenario_109',
        'scenario_110', 'scenario_111', 'scenario_112', 'scenario_113',
        'scenario_114', 'scenario_115', 'scenario_116', 'scenario_117',
        'scenario_118', 'scenario_119', 'scenario_120', 'scenario_121',
        'scenario_122', 'scenario_123', 'scenario_124', 'scenario_125',
        'scenario_126', 'scenario_127', 'scenario_128', 'scenario_129',
        'scenario_130', 'scenario_131', 'scenario_132', 'scenario_133',
        'scenario_134', 'scenario_135', 'scenario_136', 'scenario_137',
        'scenario_138', 'scenario_139', 'scenario_140',
        'scenario_batch2_1', 'scenario_batch2_2','scenario_batch2_3', 'scenario_batch2_4',
        'scenario_batch2_5', 'scenario_batch2_6', 'scenario_batch2_7','scenario_batch2_8',
        'scenario_batch2_9', 'scenario_batch2_10','scenario_batch2_11', 'scenario_batch2_12',
        'scenario_batch2_13','scenario_batch2_14','scenario_batch2_15', 'scenario_batch2_16',
        'scenario_batch2_17','scenario_batch2_18', 'scenario_batch2_19', 'scenario_batch2_20',
        'scenario_batch2_21', 'scenario_batch2_22', 'scenario_batch2_23','scenario_batch2_24',
        'scenario_batch2_25', 'scenario_batch2_26','scenario_batch2_27','scenario_batch2_28',
        'scenario_batch2_29', 'scenario_batch2_30','scenario_batch2_31', 'scenario_batch2_32',
        'scenario_batch2_33','scenario_batch2_34', 'scenario_batch2_35', 'scenario_batch2_36',
        'scenario_batch2_37', 'scenario_batch2_38','scenario_batch2_39', 'scenario_batch2_40',
        'scenario_batch2_41', 'scenario_batch2_42', 'scenario_batch2_43','scenario_batch2_44',
        'scenario_batch2_45', 'scenario_batch2_46','scenario_batch2_47', 'scenario_batch2_48',
        'scenario_batch2_49','scenario_batch2_50','scenario_batch2_51', 'scenario_batch2_52',
        'scenario_batch2_53','scenario_batch2_54', 'scenario_batch2_55', 'scenario_batch2_56',
        'scenario_batch2_57', 'scenario_batch2_58', 'scenario_batch2_59','scenario_batch2_60',
        'scenario_batch2_61', 'scenario_batch2_62','scenario_batch2_63','scenario_batch2_64',
        'scenario_batch2_65', 'scenario_batch2_66','scenario_batch2_67', 'scenario_batch2_68',
        'scenario_batch2_69','scenario_batch2_70', 'scenario_batch2_71', 'scenario_batch2_72',
        'scenario_batch2_73', 'scenario_batch2_74','scenario_batch2_75','scenario_batch2_76',
        'scenario_batch2_77', 'scenario_batch2_78', 'scenario_batch2_79','scenario_batch2_80',
        'scenario_batch2_81', 'scenario_batch2_82','scenario_batch2_83', 'scenario_batch2_84'
    ];
    var scenarios = new Array();
    var tidied_scenarios = new Array();
    scenario_names.forEach(function (scenario_name) {

        try {
            var scenario = util.readAsJSON(util.scenarioJSONPath(scenario_name));
            var host_scenario = scenario.hostScenarios[0];
            var tidied_scenario = {
                name: scenario.name,
                network_size: scenario.maximumNetworkSize,
                experiment_duration: util.convert.durationToString(scenario.experimentDuration),
                workload: util.convert.workloadToString(host_scenario.workload),
                churn: util.convert.churnToString(host_scenario.churn),
                maintenance: util.convert.maintenanceToString(host_scenario.configuration.maintenance)
            };
            scenarios.push(scenario);
            tidied_scenarios.push(tidied_scenario);
        } catch (e) {
            console.log("failed to load " + scenario_name + " : " + e);
        }
    });

    var by_churn = tidied_scenarios.groupBy("churn");
    var by_workload = tidied_scenarios.groupBy("workload");
    var by_maintenance = tidied_scenarios.groupBy("maintenance");


    $("#churn_filters").html(mark.up(util.read("templates/filter.html"), {labels:Object.keys(by_churn), property_name: 'churn'}));
    $("#workload_filters").html(mark.up(util.read("templates/filter.html"), {labels:Object.keys(by_workload), property_name: 'workload'}));
    $("#maintenance_filters").html(mark.up(util.read("templates/filter.html"), {labels:Object.keys(by_maintenance), property_name: 'maintenance'}));

    return {
        scenario_names: scenario_names,
        scenarios: scenarios,
        tidied_scenarios: tidied_scenarios,
        by_churn: by_churn,
        by_workload: by_workload,
        by_maintenance: by_maintenance
    };
});
