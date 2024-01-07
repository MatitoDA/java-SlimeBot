package de.slimecloud.slimeball.features.statistic;

import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.types.AtomicString;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class MemberCount extends ListenerAdapter {
	private final SlimeBot bot;

	public MemberCount(@NotNull SlimeBot bot) {
		this.bot = bot;
	}

	@Nullable
	private StatisticConfig getConfig(long guild) {
		return bot.loadGuild(guild).getStatistic().orElse(null);
	}

	@Nullable
	private VoiceChannel getChannel(long guild) {
		StatisticConfig config = getConfig(guild);
		if (config == null) return null;
		return bot.getJda().getVoiceChannelById(config.getMemberCountChannel());
	}

	@NotNull
	@SuppressWarnings("ConstantConditions")
	private String getFormat(long guild, @NotNull Map<String, Object> values) {
		StatisticConfig config = getConfig(guild);
		if (config == null) return null;

		AtomicString format = new AtomicString(config.getMemberCountFormat());
		if (format.isEmpty()) return values.values().toString();

		values.forEach((k, v) -> format.set(format.get().replace("%" + k + "%", String.valueOf(v))));

		return format.get();
	}

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		update(event);
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		update(event);
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		update(event);
	}

	private void update(@NotNull GenericGuildEvent event) {
		update(event.getGuild());
	}

	public void update(@NotNull Guild guild) {
		VoiceChannel channel = getChannel(guild.getIdLong());
		if (channel == null) return;

		String format = getFormat(guild.getIdLong(), Map.of("members", guild
				.getMembers()
				.stream()
				.filter(m -> !m.getUser().isBot())
				.count()));

		channel.getManager().setName(format).queue();
	}
}
