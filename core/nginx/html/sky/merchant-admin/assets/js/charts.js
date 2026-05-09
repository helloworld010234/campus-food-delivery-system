// Chart.js Configuration for Dashboard and Statistics

// Initialize charts when DOM is ready
let revenueChart = null;
let categoryChart = null;
let hourlyChart = null;

const CHART_FONT = "'Manrope', 'Noto Sans SC', 'PingFang SC', sans-serif";
const CHART_THEME = {
    primary: '#2a7d58',
    secondary: '#1d5f43',
    success: '#2e8c62',
    info: '#3ea678',
    violet: '#328f65',
    mint: '#207452',
    text: '#3e5550',
    muted: '#6d837d',
    grid: 'rgba(109, 131, 125, 0.24)',
    tooltipBg: 'rgba(17, 51, 43, 0.92)',
    tooltipText: '#d8ebe1'
};
const CATEGORY_COLORS = [CHART_THEME.primary, CHART_THEME.secondary, CHART_THEME.info, CHART_THEME.success, CHART_THEME.violet, CHART_THEME.mint];

// 创建收入趋势图
function createRevenueChart(canvasId, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;

    // 销毁旧图表
    if (revenueChart) {
        revenueChart.destroy();
    }

    revenueChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.labels,
            datasets: [{
                label: '销售额 (元)',
                data: data.revenue,
                borderColor: CHART_THEME.primary,
                backgroundColor: 'rgba(42, 125, 88, 0.16)',
                tension: 0.4,
                fill: true,
                pointRadius: 4,
                pointHoverRadius: 6,
                pointBackgroundColor: CHART_THEME.primary,
                pointBorderColor: '#fff',
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        color: CHART_THEME.text,
                        font: {
                            size: 13,
                            weight: '600',
                            family: CHART_FONT
                        }
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: CHART_THEME.tooltipBg,
                    titleColor: CHART_THEME.tooltipText,
                    bodyColor: CHART_THEME.tooltipText,
                    padding: 12,
                    titleFont: {
                        size: 13,
                        weight: '700',
                        family: CHART_FONT
                    },
                    bodyFont: {
                        size: 12,
                        family: CHART_FONT
                    },
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            label += '¥' + context.parsed.y.toFixed(2);
                            return label;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '¥' + value;
                        },
                        color: CHART_THEME.muted,
                        font: {
                            size: 11,
                            family: CHART_FONT
                        }
                    },
                    grid: {
                        color: CHART_THEME.grid
                    }
                },
                x: {
                    grid: {
                        color: CHART_THEME.grid,
                        drawOnChartArea: false
                    },
                    ticks: {
                        color: CHART_THEME.muted,
                        font: {
                            size: 11,
                            family: CHART_FONT
                        }
                    }
                }
            },
            interaction: {
                mode: 'nearest',
                axis: 'x',
                intersect: false
            }
        }
    });

    return revenueChart;
}

// 创建分类销售占比图（饼图）
function createCategoryChart(canvasId, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;

    // 销毁旧图表
    if (categoryChart) {
        categoryChart.destroy();
    }

    categoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.labels,
            datasets: [{
                data: data.values,
                backgroundColor: data.labels.map((_, i) => CATEGORY_COLORS[i % CATEGORY_COLORS.length]),
                borderColor: 'rgba(255, 255, 255, 0.95)',
                borderWidth: 2,
                hoverOffset: 10
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        padding: 15,
                        color: CHART_THEME.text,
                        font: {
                            size: 12,
                            weight: '600',
                            family: CHART_FONT
                        },
                        generateLabels: function(chart) {
                            const data = chart.data;
                            if (data.labels.length && data.datasets.length) {
                                return data.labels.map((label, i) => {
                                    const value = data.datasets[0].data[i];
                                    const total = data.datasets[0].data.reduce((a, b) => a + b, 0);
                                    const percentage = ((value / total) * 100).toFixed(1);
                                    return {
                                        text: `${label} (${percentage}%)`,
                                        fillStyle: data.datasets[0].backgroundColor[i],
                                        hidden: false,
                                        index: i
                                    };
                                });
                            }
                            return [];
                        }
                    }
                },
                tooltip: {
                    backgroundColor: CHART_THEME.tooltipBg,
                    titleColor: CHART_THEME.tooltipText,
                    bodyColor: CHART_THEME.tooltipText,
                    padding: 12,
                    titleFont: {
                        size: 13,
                        weight: '700',
                        family: CHART_FONT
                    },
                    bodyFont: {
                        size: 12,
                        family: CHART_FONT
                    },
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return `${label}: ${percentage}%`;
                        }
                    }
                }
            },
            cutout: '60%'
        }
    });

    return categoryChart;
}

// 创建时段订单量图（柱状图）
function createHourlyChart(canvasId, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;

    // 销毁旧图表
    if (hourlyChart) {
        hourlyChart.destroy();
    }

    hourlyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.labels,
            datasets: [{
                label: '订单量',
                data: data.values,
                backgroundColor: function(context) {
                    const value = context.parsed.y;
                    if (value > 50) return CHART_THEME.primary;
                    if (value > 30) return CHART_THEME.secondary;
                    if (value > 15) return CHART_THEME.success;
                    return CHART_THEME.info;
                },
                borderRadius: 7,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: CHART_THEME.tooltipBg,
                    titleColor: CHART_THEME.tooltipText,
                    bodyColor: CHART_THEME.tooltipText,
                    padding: 12,
                    titleFont: {
                        size: 13,
                        weight: '700',
                        family: CHART_FONT
                    },
                    bodyFont: {
                        size: 12,
                        family: CHART_FONT
                    },
                    callbacks: {
                        label: function(context) {
                            return '订单量: ' +context.parsed.y + ' 单';
                        },
                        title: function(context) {
                            return context[0].label + '时';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 10,
                        color: CHART_THEME.muted,
                        font: {
                            size: 11,
                            family: CHART_FONT
                        }
                    },
                    grid: {
                        color: CHART_THEME.grid
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        color: CHART_THEME.muted,
                        font: {
                            size: 10,
                            family: CHART_FONT
                        },
                        maxRotation: 45,
                        minRotation: 45
                    }
                }
            }
        }
    });

    return hourlyChart;
}

// 创建热销商品横向柱状图
function createTopProductsChart(canvasId, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;

    return new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(item => item.name),
            datasets: [{
                label: '销量',
                data: data.map(item => item.sales),
                backgroundColor: CHART_THEME.primary,
                borderRadius: 7
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: CHART_THEME.tooltipBg,
                    titleColor: CHART_THEME.tooltipText,
                    bodyColor: CHART_THEME.tooltipText,
                    padding: 12,
                    bodyFont: {
                        size: 12,
                        family: CHART_FONT
                    },
                    callbacks: {
                        label: function(context) {
                            return '销量: ' + context.parsed.x + ' 份';
                        }
                    }
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: {
                        color: CHART_THEME.muted,
                        font: {
                            size: 11,
                            family: CHART_FONT
                        }
                    },
                    grid: {
                        color: CHART_THEME.grid
                    }
                },
                y: {
                    ticks: {
                        color: CHART_THEME.muted,
                        font: {
                            size: 11,
                            family: CHART_FONT
                        }
                    },
                    grid: {
                        display: false
                    }
                }
            }
        }
    });
}

// 创建双轴图表（订单量和销售额）
function createDualAxisChart(canvasId, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;

    return new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.labels,
            datasets: [
                {
                    label: '订单量',
                    data: data.orders,
                    backgroundColor: 'rgba(17, 167, 92, 0.72)',
                    borderColor: CHART_THEME.success,
                    borderWidth: 1,
                    borderRadius: 7,
                    yAxisID: 'y'
                },
                {
                    label: '销售额',
                    data: data.revenue,
                    type: 'line',
                    borderColor: CHART_THEME.primary,
                    backgroundColor: 'rgba(42, 125, 88, 0.16)',
                    tension: 0.4,
                    fill: true,
                    yAxisID: 'y1',
                    pointRadius: 4,
                    pointHoverRadius: 6
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        color: CHART_THEME.text,
                        font: {
                            size: 12,
                            weight: '600',
                            family: CHART_FONT
                        }
                    }
                },
                tooltip: {
                    backgroundColor: CHART_THEME.tooltipBg,
                    titleColor: CHART_THEME.tooltipText,
                    bodyColor: CHART_THEME.tooltipText,
                    padding: 12,
                    bodyFont: {
                        size: 12,
                        family: CHART_FONT
                    },
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            if (context.dataset.yAxisID === 'y1') {
                                label += '¥' + context.parsed.y.toFixed(2);
                            } else {
                                label += context.parsed.y + ' 单';
                            }
                            return label;
                        }
                    }
                }
            },
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: '订单量',
                        color: CHART_THEME.muted,
                        font: {
                            size: 12,
                            family: CHART_FONT
                        }
                    },
                    ticks: {
                        color: CHART_THEME.muted,
                        font: {
                            size: 11,
                            family: CHART_FONT
                        }
                    },
                    grid: {
                        color: CHART_THEME.grid
                    }
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: '销售额 (元)',
                        color: CHART_THEME.muted,
                        font: {
                            size: 12,
                            family: CHART_FONT
                        }
                    },
                    grid: {
                        drawOnChartArea: false
                    },
                    ticks: {
                        color: CHART_THEME.muted,
                        font: {
                            size: 11,
                            family: CHART_FONT
                        },
                        callback: function(value) {
                            return '¥' + value;
                        }
                    }
                },
                x: {
                    ticks: {
                        color: CHART_THEME.muted,
                        font: {
                            size: 11,
                            family: CHART_FONT
                        }
                    },
                    grid: {
                        display: false
                    }
                }
            }
        }
    });
}

// 销毁所有图表
function destroyAllCharts() {
    if (revenueChart) {
        revenueChart.destroy();
        revenueChart = null;
    }
    if (categoryChart) {
        categoryChart.destroy();
        categoryChart = null;
    }
    if (hourlyChart) {
        hourlyChart.destroy();
        hourlyChart = null;
    }
}
