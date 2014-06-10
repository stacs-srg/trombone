define(['util', 'mark', 'mt'], function (util, mark) {

    var scenario_names = [
        'churn_4h_1', 'churn_4h_2', 'churn_4h_3', 'churn_4h_4',
        'churn_4h_5', 'churn_4h_6', 'churn_4h_7', 'churn_4h_8',
        'churn_4h_9', 'churn_4h_10', 'churn_4h_11', 'churn_4h_12',
        'churn_4h_13', 'churn_4h_14', 'churn_4h_15', 'churn_4h_16',
        'churn_4h_17', 'churn_4h_18', 'churn_population_size_4h_1', 'churn_population_size_4h_2',
        'churn_population_size_4h_3', 'churn_population_size_4h_4', 'churn_population_size_4h_5', 'churn_population_size_4h_6',
        'churn_population_size_4h_7', 'churn_population_size_4h_8', 'evolutionary_6h_1', 'evolutionary_6h_2',
        'evolutionary_6h_3', 'evolutionary_6h_4', 'evolutionary_6h_5', 'evolutionary_6h_6',
        'evolutionary_6h_7', 'evolutionary_6h_8', 'evolutionary_6h_9', 'evolutionary_6h_10',
        'evolutionary_6h_11', 'evolutionary_6h_12', 'evolutionary_6h_13', 'evolutionary_6h_14',
        'evolutionary_6h_15', 'evolutionary_6h_16', 'evolutionary_6h_17', 'evolutionary_6h_18',
        'evolutionary_6h_19', 'evolutionary_6h_20', 'evolutionary_6h_21', 'evolutionary_6h_22',
        'evolutionary_6h_23', 'evolutionary_6h_24', 'evolutionary_8h_1', 'evolutionary_8h_2',
        'evolutionary_8h_3', 'evolutionary_8h_4', 'evolutionary_8h_5', 'evolutionary_8h_6',
        'evolutionary_8h_7', 'evolutionary_8h_8', 'evolutionary_8h_9', 'evolutionary_8h_10',
        'evolutionary_8h_11', 'evolutionary_8h_12', 'evolutionary_8h_13', 'evolutionary_8h_14',
        'evolutionary_8h_15', 'evolutionary_8h_16', 'evolutionary_8h_17', 'evolutionary_8h_18',
        'evolutionary_8h_19', 'evolutionary_8h_20', 'evolutionary_8h_21', 'evolutionary_8h_22',
        'evolutionary_8h_23', 'evolutionary_8h_24', 'evolutionary_10h_1', 'evolutionary_10h_2',
        'evolutionary_10h_3', 'evolutionary_10h_4', 'evolutionary_10h_5', 'evolutionary_10h_6',
        'evolutionary_10h_7', 'evolutionary_10h_8', 'evolutionary_10h_9', 'evolutionary_10h_10',
        'evolutionary_10h_11', 'evolutionary_10h_12', 'evolutionary_10h_13', 'evolutionary_10h_14',
        'evolutionary_10h_15', 'evolutionary_10h_16', 'evolutionary_10h_17', 'evolutionary_10h_18',
        'evolutionary_10h_19', 'evolutionary_10h_20', 'evolutionary_10h_21', 'evolutionary_10h_22',
        'evolutionary_10h_23', 'evolutionary_10h_24', 'evolutionary_12h_1', 'evolutionary_12h_2',
        'evolutionary_12h_3', 'evolutionary_12h_4', 'evolutionary_12h_5', 'evolutionary_12h_6',
        'evolutionary_12h_7', 'evolutionary_12h_8', 'evolutionary_12h_9', 'evolutionary_12h_10',
        'evolutionary_12h_11', 'evolutionary_12h_12', 'evolutionary_12h_13', 'evolutionary_12h_14',
        'evolutionary_12h_15', 'evolutionary_12h_16', 'evolutionary_12h_17', 'evolutionary_12h_18',
        'evolutionary_12h_19', 'evolutionary_12h_20', 'evolutionary_12h_21', 'evolutionary_12h_22',
        'evolutionary_12h_23', 'evolutionary_12h_24', 'evolutionary_14h_1', 'evolutionary_14h_2',
        'evolutionary_14h_3', 'evolutionary_14h_4', 'evolutionary_14h_5', 'evolutionary_14h_6',
        'evolutionary_14h_7', 'evolutionary_14h_8', 'evolutionary_14h_9', 'evolutionary_14h_10',
        'evolutionary_14h_11', 'evolutionary_14h_12', 'evolutionary_14h_13', 'evolutionary_14h_14',
        'evolutionary_14h_15', 'evolutionary_14h_16', 'evolutionary_14h_17', 'evolutionary_14h_18',
        'evolutionary_14h_19', 'evolutionary_14h_20', 'evolutionary_14h_21', 'evolutionary_14h_22',
        'evolutionary_14h_23', 'evolutionary_14h_24', 'evolutionary_p_4h_1', 'evolutionary_p_4h_2',
        'evolutionary_p_4h_3', 'evolutionary_p_4h_4', 'evolutionary_p_4h_5', 'evolutionary_p_4h_6',
        'evolutionary_p_4h_7', 'evolutionary_p_4h_8', 'evolutionary_p_4h_9', 'evolutionary_p_4h_10',
        'evolutionary_p_4h_11', 'evolutionary_p_4h_12', 'evolutionary_p_4h_13', 'evolutionary_p_4h_14',
        'evolutionary_p_4h_15', 'evolutionary_p_4h_16', 'evolutionary_p_4h_17', 'evolutionary_p_4h_18',
        'evolutionary_p_4h_19', 'evolutionary_p_4h_20', 'evolutionary_p_4h_21', 'evolutionary_p_4h_22',
        'evolutionary_p_4h_23', 'evolutionary_p_4h_24', 'evolutionary_p_4h_25', 'evolutionary_p_4h_26',
        'evolutionary_p_4h_27', 'evolutionary_p_4h_28', 'evolutionary_p_4h_29', 'evolutionary_p_4h_30',
        'evolutionary_p_4h_31', 'evolutionary_p_4h_32', 'evolutionary_p_4h_33', 'evolutionary_p_4h_34',
        'evolutionary_p_4h_35', 'evolutionary_p_4h_36', 'evolutionary_p_4h_37', 'evolutionary_p_4h_38',
        'evolutionary_p_4h_39', 'evolutionary_p_4h_40', 'evolutionary_p_4h_41', 'evolutionary_p_4h_42',
        'evolutionary_p_4h_43', 'evolutionary_p_4h_44', 'evolutionary_p_4h_45', 'evolutionary_p_4h_46',
        'evolutionary_p_4h_47', 'evolutionary_p_4h_48', 'evolutionary_p_4h_49', 'evolutionary_p_4h_50',
        'evolutionary_p_4h_51', 'evolutionary_p_4h_52', 'evolutionary_p_4h_53', 'evolutionary_p_4h_54',
        'evolutionary_p_4h_55', 'evolutionary_p_4h_56', 'evolutionary_p_4h_57', 'evolutionary_p_4h_58',
        'evolutionary_p_4h_59', 'evolutionary_p_4h_60', 'evolutionary_p_4h_61', 'evolutionary_p_4h_62',
        'evolutionary_p_4h_63', 'evolutionary_p_4h_64', 'evolutionary_p_4h_65', 'evolutionary_p_4h_66',
        'evolutionary_p_4h_67', 'evolutionary_p_4h_68', 'evolutionary_p_4h_69', 'evolutionary_p_4h_70',
        'evolutionary_p_4h_71', 'evolutionary_p_4h_72', 'evolutionary_p_4h_73', 'evolutionary_p_4h_74',
        'evolutionary_p_4h_75', 'evolutionary_p_4h_76', 'evolutionary_p_4h_77', 'evolutionary_p_4h_78',
        'evolutionary_p_4h_79', 'evolutionary_p_4h_80', 'evolutionary_p_4h_81', 'evolutionary_p_4h_82',
        'evolutionary_p_4h_83', 'evolutionary_p_4h_84', 'evolutionary_p_4h_85', 'evolutionary_p_4h_86',
        'evolutionary_p_4h_87', 'evolutionary_p_4h_88', 'evolutionary_p_4h_89', 'evolutionary_p_4h_90',
        'evolutionary_p_4h_91', 'evolutionary_p_4h_92', 'evolutionary_p_4h_93', 'evolutionary_p_4h_94',
        'evolutionary_p_4h_95', 'evolutionary_p_4h_96', 'random_4h_1', 'random_4h_2',
//        'random_4h_3', 'random_4h_4', 'random_4h_5','random_4h_6',
//        'random_4h_7', 'random_4h_8', 'random_4h_9','random_4h_10',
//        'random_4h_11', 'random_4h_12', 'random_4h_13','random_4h_14',
//        'random_4h_15', 'random_4h_16', 'random_4h_17','random_4h_18',
//        'random_4h_19', 'random_4h_20', 'random_4h_21','random_4h_22',
//        'random_4h_23', 'random_4h_24', 
        'random_6h_1', 'random_6h_2',
        'random_6h_3', 'random_6h_4', 'random_6h_5', 'random_6h_6',
        'random_6h_7', 'random_6h_8', 'random_6h_9', 'random_6h_10',
        'random_6h_11', 'random_6h_12', 'random_6h_13', 'random_6h_14',
        'random_6h_15', 'random_6h_16', 'random_6h_17', 'random_6h_18',
        'random_6h_19', 'random_6h_20', 'random_6h_21', 'random_6h_22',
        'random_6h_23', 'random_6h_24', 'random_8h_1', 'random_8h_2',
        'random_8h_3', 'random_8h_4', 'random_8h_5', 'random_8h_6',
        'random_8h_7', 'random_8h_8', 'random_8h_9', 'random_8h_10',
        'random_8h_11', 'random_8h_12', 'random_8h_13', 'random_8h_14',
        'random_8h_15', 'random_8h_16', 'random_8h_17', 'random_8h_18',
        'random_8h_19', 'random_8h_20', 'random_8h_21', 'random_8h_22',
        'random_8h_23', 'random_8h_24', 'random_10h_1', 'random_10h_2',
        'random_10h_3', 'random_10h_4', 'random_10h_5', 'random_10h_6',
        'random_10h_7', 'random_10h_8', 'random_10h_9', 'random_10h_10',
        'random_10h_11', 'random_10h_12', 'random_10h_13', 'random_10h_14',
        'random_10h_15', 'random_10h_16', 'random_10h_17', 'random_10h_18',
        'random_10h_19', 'random_10h_20', 'random_10h_21', 'random_10h_22',
        'random_10h_23', 'random_10h_24', 'random_12h_1', 'random_12h_2',
        'random_12h_3', 'random_12h_4', 'random_12h_5', 'random_12h_6',
        'random_12h_7', 'random_12h_8', 'random_12h_9', 'random_12h_10',
        'random_12h_11', 'random_12h_12', 'random_12h_13', 'random_12h_14',
        'random_12h_15', 'random_12h_16', 'random_12h_17', 'random_12h_18',
        'random_12h_19', 'random_12h_20', 'random_12h_21', 'random_12h_22',
        'random_12h_23', 'random_12h_24', 'random_14h_1', 'random_14h_2',
        'random_14h_3', 'random_14h_4', 'random_14h_5', 'random_14h_6',
        'random_14h_7', 'random_14h_8', 'random_14h_9', 'random_14h_10',
        'random_14h_11', 'random_14h_12', 'random_14h_13', 'random_14h_14',
        'random_14h_15', 'random_14h_16', 'random_14h_17', 'random_14h_18',
        'random_14h_19', 'random_14h_20', 'random_14h_21', 'random_14h_22',
        'random_14h_23', 'random_14h_24', 'scenario_1', 'scenario_2',
        'scenario_3', 'scenario_4', 'scenario_5', 'scenario_6',
        'scenario_7', 'scenario_8', 'scenario_9', 'scenario_10',
        'scenario_11', 'scenario_12', 'scenario_13', 'scenario_14',
        'scenario_15', 'scenario_16', 'scenario_17', 'scenario_18',
        'scenario_19', 'scenario_20', 'scenario_21', 'scenario_22',
        'scenario_23', 'scenario_24', 'scenario_25', 'scenario_26',
        'scenario_27', 'scenario_28', 'scenario_29', 'scenario_30',
        'scenario_31', 'scenario_32', 'scenario_33', 'scenario_34',
        'scenario_35', 'scenario_36', 'scenario_37', 'scenario_38',
        'scenario_39', 'scenario_40', 'scenario_41', 'scenario_42',
        'scenario_43', 'scenario_44', 'scenario_45', 'scenario_46',
        'scenario_47', 'scenario_48', 'scenario_49', 'scenario_50',
        'scenario_51', 'scenario_52', 'scenario_53', 'scenario_54',
        'scenario_55', 'scenario_56', 'scenario_57', 'scenario_58',
        'scenario_59', 'scenario_60', 'scenario_61', 'scenario_62',
        'scenario_63', 'scenario_64', 'scenario_65', 'scenario_66',
        'scenario_67', 'scenario_68', 'scenario_69', 'scenario_70',
        'scenario_71', 'scenario_72', 'scenario_73', 'scenario_74',
        'scenario_75', 'scenario_76', 'scenario_77', 'scenario_78',
        'scenario_79', 'scenario_80', 'scenario_81', 'scenario_82',
        'scenario_83', 'scenario_84', 'scenario_85', 'scenario_86',
        'scenario_87', 'scenario_88', 'scenario_89', 'scenario_90',
        'scenario_91', 'scenario_92', 'scenario_93', 'scenario_94',
        'scenario_95', 'scenario_96', 'scenario_97', 'scenario_98',
        'scenario_99', 'scenario_100', 'scenario_101', 'scenario_102',
        'scenario_103', 'scenario_104', 'scenario_105', 'scenario_106',
        'scenario_107', 'scenario_108', 'scenario_109', 'scenario_110',
        'scenario_111', 'scenario_112', 'scenario_113', 'scenario_114',
        'scenario_115', 'scenario_116', 'scenario_117', 'scenario_118',
        'scenario_119', 'scenario_120', 'scenario_121', 'scenario_122',
        'scenario_123', 'scenario_124', 'scenario_125', 'scenario_126',
        'scenario_127', 'scenario_128', 'scenario_129', 'scenario_130',
        'scenario_131', 'scenario_132', 'scenario_133', 'scenario_134',
        'scenario_135', 'scenario_136', 'scenario_137', 'scenario_138',
        'scenario_139', 'scenario_140', 'scenario_141', 'scenario_142',
        'scenario_143', 'scenario_144', 'scenario_145', 'scenario_146',
        'scenario_147', 'scenario_148', 'scenario_149', 'scenario_150',
        'scenario_151', 'scenario_152', 'scenario_153', 'scenario_154',
        'scenario_155', 'scenario_156', 'scenario_157', 'scenario_158',
        'scenario_159', 'scenario_160', 'scenario_161', 'scenario_162',
        'scenario_163', 'scenario_164', 'scenario_165', 'scenario_166',
        'scenario_167', 'scenario_168', 'scenario_169', 'scenario_170',
        'scenario_171', 'scenario_172', 'scenario_173', 'scenario_174',
        'scenario_175', 'scenario_176', 'scenario_177', 'scenario_178',
        'scenario_179', 'scenario_180', 'scenario_181', 'scenario_182',
        'scenario_183', 'scenario_184', 'scenario_185', 'scenario_186',
        'scenario_187', 'scenario_188', 'scenario_189', 'scenario_190',
        'scenario_191', 'scenario_192', 'scenario_193', 'scenario_194',
        'scenario_195', 'scenario_196', 'scenario_197', 'scenario_198',
        'scenario_199', 'scenario_200', 'scenario_201', 'scenario_202',
        'scenario_203', 'scenario_204', 'scenario_205', 'scenario_206',
        'scenario_207', 'scenario_208', 'scenario_209', 'scenario_210',
        'scenario_211', 'scenario_212', 'scenario_213', 'scenario_214',
        'scenario_215', 'scenario_216', 'trial_time_4h_1', 'trial_time_4h_2',
        'trial_time_4h_3', 'trial_time_4h_4', 'trial_time_4h_5', 'trial_time_4h_6',
        'trial_time_4h_7', 'trial_time_4h_8', 'trial_time_4h_9', 'trial_time_4h_10',
        'trial_time_4h_11', 'trial_time_4h_12', 'trial_time_4h_13', 'trial_time_4h_14',
        'trial_time_4h_15', 'trial_time_4h_16', 'trial_time_4h_17', 'trial_time_4h_18',
        'trial_time_4h_19', 'trial_time_4h_20', 'trial_time_4h_21', 'trial_time_4h_22',
        'trial_time_4h_23', 'trial_time_4h_24',
        'evolutionary_app_feedback_4h_1', 'evolutionary_app_feedback_4h_2', 'evolutionary_app_feedback_4h_3', 'evolutionary_app_feedback_4h_4',
        'evolutionary_app_feedback_4h_5', 'evolutionary_app_feedback_4h_6', 'evolutionary_app_feedback_4h_7', 'evolutionary_app_feedback_4h_8',
        'evolutionary_app_feedback_4h_9', 'evolutionary_app_feedback_4h_10', 'evolutionary_app_feedback_4h_11', 'evolutionary_app_feedback_4h_12',
        'evolutionary_app_feedback_4h_13', 'evolutionary_app_feedback_4h_14', 'evolutionary_app_feedback_4h_15', 'evolutionary_app_feedback_4h_16',
        'evolutionary_app_feedback_4h_17', 'evolutionary_app_feedback_4h_18', 'evolutionary_app_feedback_4h_19', 'evolutionary_app_feedback_4h_20',
        'evolutionary_app_feedback_4h_21', 'evolutionary_app_feedback_4h_22', 'evolutionary_app_feedback_4h_23', 'evolutionary_app_feedback_4h_24',
        'evolutionary_app_feedback_4h_25', 'evolutionary_app_feedback_4h_26', 'evolutionary_app_feedback_4h_27', 'evolutionary_app_feedback_4h_28',
        'evolutionary_app_feedback_4h_29', 'evolutionary_app_feedback_4h_30', 'evolutionary_app_feedback_4h_31', 'evolutionary_app_feedback_4h_32',
        'evolutionary_app_feedback_4h_33', 'evolutionary_app_feedback_4h_34', 'evolutionary_app_feedback_4h_35', 'evolutionary_app_feedback_4h_36',
        'evolutionary_app_feedback_4h_37', 'evolutionary_app_feedback_4h_38', 'evolutionary_app_feedback_4h_39', 'evolutionary_app_feedback_4h_40',
        'evolutionary_random_52h_1', 'evolutionary_random_52h_2', 'evolutionary_random_52h_3', 'evolutionary_random_52h_4', 'evolutionary_random_52h_5',
        'evolutionary_random_52h_6', 'evolutionary_random_52h_7', 'evolutionary_random_52h_8',
        'config_space_size_4h_1', 'config_space_size_4h_2', 'config_space_size_4h_3', 'config_space_size_4h_4', 'config_space_size_4h_5',
        'config_space_size_4h_6', 'config_space_size_4h_7', 'config_space_size_4h_8', 'config_space_size_4h_9', 'config_space_size_4h_10',
        'config_space_size_4h_11', 'config_space_size_4h_12', 'config_space_size_4h_13', 'config_space_size_4h_14', 'config_space_size_4h_15',
        'config_space_size_4h_16', 'config_space_size_4h_17', 'config_space_size_4h_18', 'config_space_size_4h_19', 'config_space_size_4h_20',
        'config_space_size_4h_21', 'config_space_size_4h_22', 'config_space_size_4h_23', 'config_space_size_4h_24', 'config_space_size_4h_25',
        'config_space_size_4h_26', 'config_space_size_4h_27', 'config_space_size_4h_28', 'config_space_size_4h_29', 'config_space_size_4h_30',
        'config_space_size_4h_31', 'config_space_size_4h_32', 'config_space_size_4h_33', 'config_space_size_4h_34', 'config_space_size_4h_35',
        'config_space_size_4h_36', 'config_space_size_4h_37', 'config_space_size_4h_38', 'config_space_size_4h_39', 'config_space_size_4h_40',
        'config_space_size_4h_41', 'config_space_size_4h_42', 'config_space_size_4h_43', 'config_space_size_4h_44', 'config_space_size_4h_45',
        'config_space_size_4h_46', 'config_space_size_4h_47', 'config_space_size_4h_48'
    ];
    var scenarios = new Array();
    var tidied_scenarios = new Array();
    var by_churn;
    var by_workload;
    var by_maintenance;
    var by_experiment_duration;

    var parallel = new Multithread(5);
    var origin = window.location.origin + '/t3/evaluation';
    var getAllScenarios = parallel.process(util.read, function (result) {

        $('#status').text('processing ' + tidied_scenarios.length + ' of ' + scenario_names.length + ' scenarios');
        var scenario = $.parseJSON(result);
        var host_scenario = scenario.hostScenarios[0];

        var tidied_scenario = {
            name: scenario.name,
            network_size: scenario.maximumNetworkSize,
            experiment_duration: util.convert.durationToString(scenario.experimentDuration),
            workload: util.convert.workloadToString(host_scenario.workload),
            churn: util.convert.churnToString(host_scenario.churn),
            maintenance: util.convert.maintenanceToString(host_scenario.configuration.maintenance, scenario)
        };

        scenarios.push(scenario);
        tidied_scenarios.push(tidied_scenario);

        if (tidied_scenarios.length == scenario_names.length) {
            by_churn = tidied_scenarios.groupBy("churn");
            by_workload = tidied_scenarios.groupBy("workload");
            by_maintenance = tidied_scenarios.groupBy("maintenance");
            by_experiment_duration = tidied_scenarios.groupBy("experiment_duration");

            $("#churn_filters").html(mark.up(util.read("templates/filter.html"), {labels: Object.keys(by_churn), property_name: 'churn'}));
            $("#workload_filters").html(mark.up(util.read("templates/filter.html"), {labels: Object.keys(by_workload), property_name: 'workload'}));
            $("#maintenance_filters").html(mark.up(util.read("templates/filter.html"), {labels: Object.keys(by_maintenance), property_name: 'maintenance'}));
            $("#experiment_duration_filters").html(mark.up(util.read("templates/filter.html"), {labels: Object.keys(by_experiment_duration), property_name: 'experiment_duration'}));
            $('#status').text('');
        }
    });


    scenario_names.forEach(function (scenario_name) {

        getAllScenarios(origin + '/results/' + scenario_name + '/scenario.json');

    });

    return {
        scenario_names: scenario_names,
        scenarios: function () {
            return scenarios;
        },
        tidied_scenarios: function () {
            return tidied_scenarios
        },
        by_churn: function () {
            return by_churn
        },
        by_workload: function () {
            return by_workload
        },
        by_maintenance: function () {
            return by_maintenance
        },
        by_experiment_duration: function () {
            return by_experiment_duration
        }
    };
});
