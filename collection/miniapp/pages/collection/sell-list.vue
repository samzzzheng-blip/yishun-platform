<template>
	<s-layout
		title="我的在售"
		navbar="normal"
		:dark="false"
		:bgStyle="{ color: 'var(--ys-page-bg)' }"
	>
		<SMySellList />
	</s-layout>
</template>

<script setup>
import SMySellList from '@/sheep/components/s-my-sell-list/s-my-sell-list.vue';
</script>
