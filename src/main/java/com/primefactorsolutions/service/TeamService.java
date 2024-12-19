package com.primefactorsolutions.service;

import com.primefactorsolutions.model.Team;
import com.primefactorsolutions.repositories.TeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;

    public void saveTeam(final Team newTeam) {
        teamRepository.save(newTeam);
    }

    public void deleteTeam(final UUID id) {
        teamRepository.deleteById(id);
    }

    public List<Team> findAllTeams() {
        return teamRepository.findAll();
    }
}
